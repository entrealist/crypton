package cc.cryptopunks.crypton.repo

import androidx.paging.DataSource
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.validate
import cc.cryptopunks.crypton.entity.MessageData
import cc.cryptopunks.crypton.entity.message
import cc.cryptopunks.crypton.entity.messageData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MessageRepo(
    private val dao: MessageData.Dao,
    override val coroutineContext: CoroutineContext
) : Message.Repo,
    CoroutineScope {

    private var latest: Message = Message.Empty
        set(value) {
            if (field.timestamp < value.timestamp)
                field = value
        }

    private val unreadMessagesChannel = BroadcastChannel<List<Message>>(Channel.CONFLATED)

    init {
        launch {
            notifyUnread()
        }
    }

    override suspend fun insertOrUpdate(message: Message) {
        latest = message.validate()
        dao.insertOrUpdate(message.messageData())
    }

    override suspend fun insertOrUpdate(messages: List<Message>) {
        dao.insertOrUpdate(messages.map {
            latest = it.validate()
            it.messageData()
        })
    }

    override suspend fun notifyUnread() {
        val unread = listUnread()
        unreadMessagesChannel.send(unread)
    }

    override suspend fun get(id: String): Message? =
        dao.get(id)?.message()

    override suspend fun delete(id: String) =
        dao.delete(id)

    override suspend fun delete(message: Message) =
        dao.delete(message.id)

    override suspend fun delete(messages: List<Message>) {
        messages.forEach { dao.delete(it.id) }
    }

    override suspend fun latest(): Message? =
        if (latest != Message.Empty)
            latest else
            dao.latest()
                ?.message()
                ?.also { latest = it }

    override suspend fun latest(chat: Address): Message? =
        dao.latest(chat.id)
            ?.message()
            ?.also { latest = it }

    override suspend fun listUnread(): List<Message> =
        dao.listUnread().map { it.message() }

    override suspend fun list(chat: Address?, range: LongRange): List<Message> = if (chat == null)
        dao.list(oldest = range.first, latest = range.last).map { it.message() } else
        dao.list(oldest = range.first, latest = range.last, chat = chat.id).map { it.message() }


    override suspend fun list(chat: Address, status: Message.Status): List<Message> =
        dao.list(chat.id, status.name).map { it.message() }

    override fun flowLatest(chatAddress: Address?): Flow<Message> =
        dao.run {
            if (chatAddress == null) flowLatest()
            else flowLatest(chatAddress.id)
        }.filterNotNull()
            .distinctUntilChanged()
            .map { it.message() }

    override fun dataSourceFactory(chatAddress: Address): DataSource.Factory<Int, Message> =
        dao.dataSourceFactory(chatAddress.id).map { it.message() }

    override fun flowListUnread(): Flow<List<Message>> =
        unreadMessagesChannel.asFlow().distinctUntilChanged()

    override suspend fun listQueued(): List<Message> =
        dao.listQueued().map(MessageData::message)

    override fun flowListQueued(): Flow<List<Message>> =
        dao.flowQueueList()
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .map { it.map(MessageData::message) }


    override fun flowUnreadCount(chatAddress: Address): Flow<Int> =
        flowListUnread().map { list ->
            list.filter { message ->
                message.chat == chatAddress
            }.size
        }.distinctUntilChanged()
}
