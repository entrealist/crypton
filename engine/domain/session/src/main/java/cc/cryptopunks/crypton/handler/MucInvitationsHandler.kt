package cc.cryptopunks.crypton.handler

import cc.cryptopunks.crypton.context.Chat
import cc.cryptopunks.crypton.context.SessionScope
import cc.cryptopunks.crypton.context.asChat
import cc.cryptopunks.crypton.context.handle
import cc.cryptopunks.crypton.context.insertChat

internal fun SessionScope.handleMucInvitations() =
    handle<Chat.Net.MucInvitation> {
        log.d(it)
        if (!chatRepo.contains(address)) insertChat(
            Chat.Service.CreateChat(
                account = this@handleMucInvitations.address,
                chat = address
            ).asChat()
        )
    }