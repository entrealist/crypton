package cc.cryptopunks.crypton.domain.interactor

import cc.cryptopunks.crypton.entity.Account
import cc.cryptopunks.crypton.entity.Conversation
import cc.cryptopunks.crypton.entity.Message
import cc.cryptopunks.crypton.util.Scopes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class LoadMessagesInteractor @Inject constructor(
    messageDao: Message.Dao,
    conversationDao: Conversation.Dao,
    accountDao: Account.Dao,
    scope: Scopes.UseCase
) : () -> Job by {
    //TODO: replace mock witch integration
    scope.launch {
        accountDao.list().firstOrNull()?.run {
            conversationDao.deleteAll()
            (1L..200).asFlow().collect {
                conversationDao.insertIfNeeded(
                    Conversation(
                        id = it,
                        accountId = id,
                        title = "Conversation $it"
                    )
                )

                messageDao.insertOrUpdate(
                    Message(
                        id = it.toString(),
                        conversationId = it,
                        text = "message $it"
                    )
                )
                delay(50)
            }
        }
    }
}