package cc.cryptopunks.crypton.core.service

import cc.cryptopunks.crypton.api.ApiServiceManager
import cc.cryptopunks.crypton.core.Core
import cc.cryptopunks.crypton.service.ReconnectAccountsService
import cc.cryptopunks.crypton.service.ToggleIndicatorService
import cc.cryptopunks.crypton.service.UpdateCurrentApiService
import cc.cryptopunks.crypton.util.log
import javax.inject.Inject

class CoreService @Inject constructor(
    private val toggleIndicatorService: ToggleIndicatorService,
    private val reconnectAccountsService: ReconnectAccountsService,
    private val updateCurrentApiService: UpdateCurrentApiService,
    private val apiServiceManager: ApiServiceManager

) : () -> Unit by {
    log<CoreService>("start")
    toggleIndicatorService()
    reconnectAccountsService()
    updateCurrentApiService()
    apiServiceManager()
    log<CoreService>("stop")
} {


    @dagger.Component(dependencies = [Core.Component::class])
    interface Component {
        val coreService: CoreService
    }
}