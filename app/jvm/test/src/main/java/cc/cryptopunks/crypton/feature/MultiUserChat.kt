package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.Client1
import cc.cryptopunks.crypton.Client2
import cc.cryptopunks.crypton.Client3
import cc.cryptopunks.crypton.address1
import cc.cryptopunks.crypton.address2
import cc.cryptopunks.crypton.address3
import cc.cryptopunks.crypton.chatAddress
import cc.cryptopunks.crypton.connectDslClient
import cc.cryptopunks.crypton.context.Chat
import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.Presence
import cc.cryptopunks.crypton.context.Roster
import cc.cryptopunks.crypton.context.Route
import cc.cryptopunks.crypton.context.Subscribe
import cc.cryptopunks.crypton.context.inContext
import cc.cryptopunks.crypton.createChat
import cc.cryptopunks.crypton.openChat
import cc.cryptopunks.crypton.pass
import cc.cryptopunks.crypton.prepare
import cc.cryptopunks.crypton.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun testMultiUserChat() {
    runBlocking {
        listOf(
            launch { client1() },
            launch { client2() },
            launch { client3() }
        ).joinAll()
        println("Finish multi user chat test")
    }
}

private suspend fun client1() = Client1.connectDslClient {
    prepare(address1, pass)

    createChat(address1, address2)
    openChat(address1, address2)

    send(Exec.EnqueueMessage("yo 1-2"))
    flush()

    send(Route.Main)
    flush()

    createChat(address1, address3)
    openChat(address1, address3)

    send(Exec.EnqueueMessage("yo 1-3"))
    flush()

    send(
        Route.Main,
        Subscribe.RosterItems(true, address1)
    )

    waitFor<Roster.Items> {
        list.filter { it.account == address1 }.run {
            isNotEmpty() && any {
                it.message.from.address == address1
                    && it.message.status == Message.Status.Sent
                    && it.chatAddress == address2
            } && any {
                it.message.from.address == address1
                    && it.message.status == Message.Status.Sent
                    && it.chatAddress == address3
            }
        }
    }

    waitFor<Roster.Items> {
        list.any {
            it.chatAddress == chatAddress && it.presence == Presence.Status.Unavailable
        }
    }
    send(Exec.JoinChat.inContext(address1, chatAddress))
    flush()

    delay(1000)
    openChat(address1, chatAddress)
    send(Exec.EnqueueMessage("yolo"))
    flush()

    delay(1000)
}

private suspend fun client2() = Client2.connectDslClient {
    prepare(address2, pass)

    send(Subscribe.RosterItems(true, address2))
    waitFor<Roster.Items> {
        list.any {
            it.chatAddress == address1 && it.presence == Presence.Status.Subscribe
        }
    }

    send(Exec.JoinChat.inContext(address2, address1))
    waitFor<Roster.Items> {
        list.any {
            it.chatAddress == address3 && it.presence == Presence.Status.Subscribe
        }
    }

    send(Exec.JoinChat.inContext(address2, address3))
    waitFor<Roster.Items> {
        list.any {
            it.chatAddress == chatAddress && it.presence == Presence.Status.Unavailable
        }
    }

    send(Exec.JoinChat.inContext(address2, chatAddress))
    waitFor<Roster.Items> {
        list.any {
            it.chatAddress == chatAddress
                && it.message.text == "yolo"
        }
    }

    delay(1000)
}

private suspend fun client3() = Client3.connectDslClient {
    prepare(address3, pass)

    send(Subscribe.RosterItems(true, address3))
    waitFor<Roster.Items> {
        list.filter { it.account == address3 }.run {
            isNotEmpty() && any {
                it.chatAddress == address1
                    && it.presence == Presence.Status.Subscribe
            }
        }
    }

    send(Exec.JoinChat.inContext(address3, address1))
    flush()

    createChat(address3, address2)
    openChat(address3, address2)

    send(Exec.EnqueueMessage("yo 3-2"))
    flush()

    send(
        Route.Main,
        Subscribe.RosterItems(true, address3)
    )
    waitFor<Roster.Items> {
        list.isNotEmpty() && list.any {
            it.chatAddress == address2
                && it.message.from.address == address3
                && it.message.status == Message.Status.Sent
        }
    }

    send(Exec.CreateChat(Chat(chatAddress, address3, listOf(address1, address2))))
    waitFor<Roster.Items> {
        list.any {
            it.chatAddress == chatAddress
                && it.message.text == "yolo"
        }
    }

    delay(1000)
}
