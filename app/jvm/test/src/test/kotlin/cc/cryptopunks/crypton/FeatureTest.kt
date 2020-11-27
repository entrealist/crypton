package cc.cryptopunks.crypton

import cc.cryptopunks.crypton.feature.testDirectMessaging
import cc.cryptopunks.crypton.feature.testMultiUserChat
import cc.cryptopunks.crypton.util.logger.CoroutineLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

@Ignore
class FeatureTest {

    @Test
    fun `as a user I can use direct messages`() = runBlocking {
        testDirectMessaging()
    }

    @Test
    @Ignore("Feature is not finished")
    fun `as a user I can use multi user chat`() = runBlocking {
        testMultiUserChat()
    }

    @After
    fun tearDown() = runBlocking {
        section("BEGIN CLEANING AFTER TEST")
        connectDslClient {
            removeAccounts(
                address1,
                address2,
                address3
            )
        }
        delay(2000)
        section("END CLEANING AFTER TEST")
    }

    companion object {
        private val server = TestServer()

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            section("STARTING SERVER")
            runBlocking(CoroutineLog.Label("BeforeClass")) {
                TestServer().apply {
                    start()
                    section("BEGIN CLEANING BEFORE TESTS")
                    listOf(
                        launch { connectDslClient { tryRemoveAccount(address1, pass) } },
                        launch { connectDslClient { tryRemoveAccount(address2, pass) } },
                        launch { connectDslClient { tryRemoveAccount(address3, pass) } }
                    ).joinAll()
                    section("END CLEANING BEFORE TESTS")
                    stop()
                }
                delay(3000)
                server.start()
            }
        }

        @AfterClass
        @JvmStatic
        fun afterAll() = server.stop()
    }
}

private fun section(message: String) {
    val decor = (0..(SECTION_WIDTH - message.length) / 2).joinToString("") { "=" }
    println("$decor $message $decor")
}

private const val SECTION_WIDTH = 120
