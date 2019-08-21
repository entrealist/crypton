package cc.cryptopunks.crypton.account.domain.query

import cc.cryptopunks.crypton.core.entity.Account
import io.reactivex.Observable
import javax.inject.Inject

class ObserveAccountList @Inject constructor(
    private val dao: Account.Dao
) : () -> Observable<List<Account>> by {
    dao.observeAll()
}