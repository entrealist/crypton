package cc.cryptopunks.crypton.entity

import cc.cryptopunks.crypton.manager.BaseManager
import kotlinx.coroutines.flow.Flow

data class Account constructor(
    val address: Address = Address.Empty,
    val status: Status = Status.Disconnected,
    val password: String = "",
    val updateAt: Long = System.currentTimeMillis(),
    val current: Boolean = true
) {

    val domain get() = address.domain

    enum class Status {
        Disconnected,
        Connecting,
        Connected,
    }

    data class Exception(
        val account: Account,
        override val cause: Throwable
    ) : kotlin.Exception(

        account.toString(),
        cause
    )

    @Suppress("NOTHING_TO_INLINE")
    inline fun exception(throwable: Throwable): Exception =
        if (throwable is Exception) throwable
        else Exception(this, throwable)

    interface Api {
        val address: Address

        val isConnected: IsConnected
        val connect: Connect
        val disconnect: Disconnect
        val initOmemo: InitOmemo
        val createAccount: Create
        val login: Login
        val remove: Remove
        val isAuthenticated: IsAuthenticated

        interface Connect : () -> Unit
        interface Disconnect : () -> Unit
        interface IsConnected : () -> Boolean
        interface InitOmemo : () -> Unit
        interface Create: () -> Unit
        interface Login: () -> Unit
        interface Remove: () -> Unit
        interface IsAuthenticated: () -> Boolean

        class Manager(manager: BaseManager<Account, Api>) : BaseManager<Account, Api> by manager
    }

    interface Repo {
        suspend fun contains(address: Address): Boolean
        suspend fun get(address: Address): Account
        suspend fun insert(account: Account): Account
        suspend fun update(account: Account)
        suspend fun delete(account: Account)
        suspend fun list(): List<Account>
        fun flowList(): Flow<List<Account>>
    }

    companion object {
        val Empty = Account()
    }
}