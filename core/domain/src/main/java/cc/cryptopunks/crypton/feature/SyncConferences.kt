package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.SessionScope
import cc.cryptopunks.crypton.emitter
import cc.cryptopunks.crypton.feature
import cc.cryptopunks.crypton.interactor.syncConferencesWithRetry
import cc.cryptopunks.crypton.selector.accountAuthenticatedFlow
import kotlinx.coroutines.flow.map

internal fun syncConferences() = feature(

    emitter = emitter<SessionScope> {
        accountAuthenticatedFlow().map { Exec.SyncConferences }
    },

    handler = { out, _: Exec.SyncConferences ->
        syncConferencesWithRetry(out)
    }
)
