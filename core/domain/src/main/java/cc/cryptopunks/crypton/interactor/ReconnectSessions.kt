package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.context.RootScope
import cc.cryptopunks.crypton.context.SessionScope
import cc.cryptopunks.crypton.util.logger.log
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun RootScope.reconnectSessions(): List<Job> =
    sessions.get().values.map { session ->
        launch { session.reconnectIfNeeded() }
    }

private suspend fun SessionScope.reconnectIfNeeded(): Unit = try {
    log.d { "reconnecting: $address" }
    if (!isConnected()) {
        connect()
        if (!isAuthenticated()) login()
        initOmemo()
    }
    Unit
} catch (e: Throwable) {
    log.d { "reconnection failed: $e" }
    interrupt()
}