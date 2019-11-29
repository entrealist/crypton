package cc.cryptopunks.crypton.entity

import androidx.paging.DataSource
import androidx.room.*
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Chat
import cc.cryptopunks.crypton.context.User

@Entity(
    tableName = "chat",
    indices = [Index("accountId")],
    foreignKeys = [ForeignKey(
        entity = AccountData::class,
        parentColumns = ["id"],
        childColumns = ["accountId"],
        onDelete = ForeignKey.CASCADE
    )]
)
internal data class ChatData(
    @PrimaryKey val id: AddressData,
    val accountId: AddressData,
    val title: String
) {

    @androidx.room.Dao
    interface Dao {

        @Query("select * from chat where id = :id")
        suspend fun get(id: AddressData): ChatData

        @Query("select * from chat")
        fun dataSourceFactory(): DataSource.Factory<Int, ChatData>

        @Insert
        suspend fun insert(data: ChatData)

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun insertIfNeeded(data: ChatData)

        @Delete
        suspend fun delete(data: ChatData)

        @Query("delete from chat")
        suspend fun deleteAll()

        @Query("select id from chat where id = :id")
        suspend fun contains(id: AddressData): AddressData?

        @Query("select * from chat where id in (:ids)")
        suspend fun list(ids: List<AddressData>): List<ChatData>
    }
}

internal fun ChatData.toDomain(users: List<User> = emptyList()) =
    Chat(
        title = title,
        address = Address.from(id),
        users = users
    )

internal fun Chat.chatData() = ChatData(
    title = title,
    id = address.id,
    accountId = account.id
)