package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Service
import cc.cryptopunks.crypton.manager.AccountManager
import cc.cryptopunks.crypton.util.JobManager
import cc.cryptopunks.crypton.util.typedLog
import kotlinx.coroutines.Job
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReconnectAccountsInteractor @Inject constructor(
    private val scope: Service.Scope,
    private val accountManager: AccountManager
) : () -> Job, (Address) -> Job {

    private val log = typedLog()

    private val reconnectIfNeeded = JobManager<AccountManager>(scope, log) {
        session.scope.launch {
            log.d("reconnecting: ${session.address}")
            if (isConnected) disconnect()
            connect()
            if (!isAuthenticated) login()
            initOmemo()
        }
    }

    val isWorking get() = reconnectIfNeeded.isWorking

    override fun invoke(address: Address): Job = scope.launch {
        reconnectIfNeeded.send(accountManager.copy(address = address))
    }

    override fun invoke() = scope.launch {
        accountManager.all().forEach { manager ->
            reconnectIfNeeded.send(manager)
        }
    }
}