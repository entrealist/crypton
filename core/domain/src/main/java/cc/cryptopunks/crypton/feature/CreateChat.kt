package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.cliv2.command
import cc.cryptopunks.crypton.cliv2.config
import cc.cryptopunks.crypton.cliv2.param
import cc.cryptopunks.crypton.context.Account
import cc.cryptopunks.crypton.context.Chat
import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.address
import cc.cryptopunks.crypton.context.isConference
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.inContext
import cc.cryptopunks.crypton.interactor.createChat

internal fun createChat() = feature(

    command = command(
        config("account"),
        param().copy(name = "local@domain"),
        name = "create chat",
        description = "Open chat with given address."
    ) { (account, address) ->
        Exec.CreateChat(Chat(address(address), address(account))).inContext(account)
    },

    handler = { output, (chat): Exec.CreateChat ->
        sessions[chat.account]!!.createChat(chat)
        if (!chat.address.isConference && !iAmSubscribed(chat.address))
            subscribe(chat.address)
        output(Account.ChatCreated(chat.address))
    }
)