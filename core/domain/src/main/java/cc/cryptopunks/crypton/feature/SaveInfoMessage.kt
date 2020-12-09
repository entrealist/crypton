package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.Resource
import cc.cryptopunks.crypton.context.calculateId
import cc.cryptopunks.crypton.feature

internal fun saveInfoMessage() = feature(
    handler = { _, (text): Exec.SaveInfoMessage ->
        messageRepo.insertOrUpdate(
            Message(
                body = text,
                chat = chat.address,
                type = Message.Type.Info,
                to = Resource(account.address),
                from = Resource.Empty,
                timestamp = System.currentTimeMillis()
            ).calculateId()
        )
    }
)
