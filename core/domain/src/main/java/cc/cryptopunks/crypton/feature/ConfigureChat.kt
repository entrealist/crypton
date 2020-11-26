package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.cliv2.command
import cc.cryptopunks.crypton.cliv2.config
import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.inContext


internal fun configureChat() = feature(

    command = command(
        config("account"),
        config("chat"),
        name = "configure conference",
        description = "Configure conference (WIP)."
    ) { (account, chat) ->
        Exec.ConfigureConference().inContext(account, chat)
    },

    handler = { _, _: Exec.ConfigureConference ->
        if (chat.isConference)
            configureConference(chat.address)
    }
)