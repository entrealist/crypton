package cc.cryptopunks.crypton.repo

import cc.cryptopunks.crypton.context.Account
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.entity.AccountData
import cc.cryptopunks.crypton.entity.accountData
import cc.cryptopunks.crypton.entity.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepo(
    val dao: AccountData.Dao,
) : Account.Repo {

    override suspend fun contains(address: Address): Boolean =
        dao.contains(address.id) != null

    override suspend fun get(address: Address): Account =
        requireNotNull(dao.get(address.id)).toDomain()

    override suspend fun insert(account: Account): Account =
        account.apply { dao.insert(accountData()) }

    override suspend fun update(account: Account): Unit =
        dao.update(account.accountData())

    override suspend fun delete(address: Address): Unit =
        dao.delete(address.toString())

    override suspend fun list(): List<Account> =
        dao.list().map { it.toDomain() }

    override suspend fun addressList(): List<Address> =
        dao.list().map { Address.from(it.id) }

    override fun flowList(): Flow<List<Address>> =
        dao.flowList().map { list ->
            list.map {
                Address.from(it.id)
            }
        }
}
