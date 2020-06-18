package cc.cryptopunks.crypton.service

import cc.cryptopunks.crypton.context.Connectable
import cc.cryptopunks.crypton.context.Connector
import cc.cryptopunks.crypton.context.SessionScope
import cc.cryptopunks.crypton.context.createHandlers
import cc.cryptopunks.crypton.context.dispatch
import cc.cryptopunks.crypton.context.plus
import cc.cryptopunks.crypton.handler.handleCreateChat
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CreateChatService(
    sessionScope: SessionScope
) : SessionScope by sessionScope,
    Connectable {

    private val handlers by lazy {
        createHandlers {
            plus(handleCreateChat(navigate))
        }
    }

    override fun Connector.connect(): Job = launch {
        input.collect { handlers.dispatch(it, output) }
    }
}
