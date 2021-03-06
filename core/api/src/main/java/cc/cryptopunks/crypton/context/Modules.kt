package cc.cryptopunks.crypton.context

import cc.cryptopunks.crypton.Connectable
import cc.cryptopunks.crypton.Context
import cc.cryptopunks.crypton.HandlerRegistry
import cc.cryptopunks.crypton.Scope
import cc.cryptopunks.crypton.util.Executors
import cc.cryptopunks.crypton.util.IOExecutor
import cc.cryptopunks.crypton.util.MainExecutor
import cc.cryptopunks.crypton.util.Store
import cc.cryptopunks.crypton.util.logger.CoroutineLog
import cc.cryptopunks.crypton.util.logger.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class RootModule(
    val sys: Sys,
    val repo: Repo,
    override val mainClass: KClass<*>,
    override val handlers: HandlerRegistry,
    override val createConnection: Connection.Factory,
    override val mainExecutor: MainExecutor,
    override val ioExecutor: IOExecutor,
    override val navigateChatId: Int = 0
) :
    RootScope,
    Executors,
    Sys by sys,
    Repo by repo {

    override val coroutineContext: CoroutineContext = SupervisorJob() +
        Dispatchers.IO +
        CoroutineLog.Label(javaClass.simpleName)

    override val sessions = SessionScope.Store()
    override val clipboardStore = Clip.Board.Store()
    override val connectableBindingsStore = Connectable.Binding.Store()
    override val accounts = Store(Account.Many(emptySet()))
    override val rosterItems = Store(Roster.Items(emptyList()))

    init {
        coroutineContext[Job]!!.invokeOnCompletion {
            coroutineContext.log.d { "Finish AppModule $this" }
        }
    }

    override fun sessionScope(): SessionScope = sessions.get().values.first()
    override fun sessionScope(address: Address): SessionScope =
        sessions[address] ?: throw Exception(
            "Cannot resolve SessionScope for $address\n" +
                "available sessions: ${sessions.get().keys.joinToString("\n")}"
        )

    override suspend fun resolve(context: Context): Pair<Scope, Any> =
        sessionScope(address(context.id)).let { scope ->
            when (val any = context.next) {
                is Context -> scope.resolve(any)
                else -> scope to any
            }
        }
}

class SessionModule(
    override val rootScope: RootScope,
    val connection: Connection,
    val sessionRepo: SessionRepo,
    override val address: Address,
    val onClose: (Throwable?) -> Unit = {}
) :
    SessionScope,
    RootScope by rootScope,
    Net by connection,
    SessionRepo by sessionRepo {

    override val coroutineContext: CoroutineContext = SupervisorJob(rootScope.coroutineContext[Job]) +
        newSingleThreadContext(address.id) +
        CoroutineLog.Label(javaClass.simpleName) +
        CoroutineLog.Scope(address.id)

    override val presenceStore = Presence.Store()
    override val subscriptions = Store(emptySet<Address>())

    init {
        setDeviceFingerprintRepo(deviceRepo)
        coroutineContext[Job]!!.invokeOnCompletion {
            onClose(it)
            coroutineContext.log.d { "Finish SessionModule $address ${it.hashCode()} $it" }
        }
    }

    override fun chatScope(chat: Chat): ChatScope = ChatModule(this, chat)
    override suspend fun chatScope(chat: Address): ChatScope = chatScope(chatRepo.get(chat))


    override suspend fun resolve(
        context: Context
    ): Pair<Scope, Any> = when {
        context.id == address.id -> when (val any = context.next) {
            is Context -> resolve(any)
            else -> this to context.next
        }
        chatRepo.contains(address(context.id)) -> chatScope(address(context.id)).resolve(context)
        else -> rootScope.resolve(context)
    }
}

class ChatModule(
    private val sessionScope: SessionScope,
    override val chat: Chat
) :
    SessionScope by sessionScope,
    ChatScope {

    override val pagedMessage: Store<Chat.PagedMessages?> = Store(null)

    override val coroutineContext = sessionScope.coroutineContext +
        CoroutineLog.Label(javaClass.simpleName) +
        CoroutineLog.Scope(chat.address.id)

    @Suppress("IntroduceWhenSubject")
    override suspend fun resolve(
        context: Context
    ): Pair<Scope, Any> = when {
        context.id == chat.address.id -> this to context.next
        else -> sessionScope.resolve(context)
    }
}
