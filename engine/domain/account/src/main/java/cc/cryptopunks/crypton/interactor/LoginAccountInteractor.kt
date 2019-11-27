package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.manager.AccountManager
import cc.cryptopunks.crypton.service.Service
import cc.cryptopunks.crypton.util.Log
import cc.cryptopunks.crypton.util.d
import kotlinx.coroutines.Job
import javax.inject.Inject

class LoginAccountInteractor @Inject constructor(
    private val scope: Service.Scope,
    private val manager: AccountManager
) : (Address) -> Job {

    override fun invoke(account: Address): Job = scope.launch {
        suspend(account)
    }

    internal suspend fun suspend(account: Address) {
        Log.d<LoginAccountInteractor>("login")
        manager.copy(account).run {
            if (!isConnected) connect()
            if (!isAuthenticated) login()
            initOmemo()
        }
    }
}