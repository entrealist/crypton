package cc.cryptopunks.crypton

import cc.cryptopunks.crypton.util.logger.CoroutineLog
import cc.cryptopunks.crypton.util.logger.coroutineLog
import cc.cryptopunks.crypton.util.logger.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert

class ClientDsl internal constructor(
    name: String,
    private val connector: Connector
) : CoroutineScope {
    override val coroutineContext = Job() + newSingleThreadContext(javaClass.simpleName) + CoroutineLog.Label(name)
    private val input = BroadcastChannel<Any>(Channel.BUFFERED)
    private var subscription: ReceiveChannel<Any> = Channel()
    private val actions = Channel<() -> Job>(Channel.BUFFERED)
    val log = coroutineContext.log
    val expected = ExpectedTraffic(log)

    init {
        launch {
            connector.input.onCompletion {
                log.d { "Close client $it" }
            }.collect {
                expected.check(it)
                input.send(it)
            }
        }
    }

    fun printTraffic() {
        log.d { "TRAFFIC" }
        expected.traffic.forEach {
            log.d { (it.toString()) }
        }
    }

    fun invoke(block: ClientDsl.() -> Unit) {
        subscription = input.openSubscription()
        block()
    }

    fun output() = when {
//        subscription.isClosedForReceive.not() -> subscription TODO
        subscription.isClosedForReceive.not() -> input.openSubscription()
        else -> input.openSubscription()
    }.consumeAsFlow()

    fun openSubscription() {
        if (subscription.isClosedForReceive) throw Exception()
        subscription = input.openSubscription()
    }

    fun send(vararg any: Any) = apply {
        actions.offer {
            launch {
                any.forEach {
                    expected.check(it)
                    connector.output(it)
                }
            }
        }
    }

    suspend fun flush() {
        while (true) {
            actions.poll()
                ?.invoke()
                ?.join()
                ?: break
        }
    }

    suspend inline fun <reified T> waitFor(
        timeout: Long = 120_000,
        crossinline filter: T.() -> Boolean = { true }
    ): T = withTimeout(timeout) {
        flush()
        output().filterIsInstance<T>().first { it.filter() }
    }

    suspend fun expect(vararg expected: Any, filter: suspend (Any) -> Boolean = { true }) {
        flush()
        require(expected.isNotEmpty()) { "expected cannot be empty " }
        output().filter(filter).scan(expected.toList()) { rest, value ->
            require(rest.isNotEmpty()) { "Not expect more elements but was $value" }
            Assert.assertEquals(rest.first(), value)
            rest.drop(1)
        }.takeWhile { it.isNotEmpty() }.collect()
    }
}
