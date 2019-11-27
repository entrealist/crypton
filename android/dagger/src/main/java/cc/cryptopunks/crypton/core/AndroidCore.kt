package cc.cryptopunks.crypton.core

import android.app.Application
import android.app.NotificationManager
import android.content.ClipboardManager
import android.net.ConnectivityManager
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.core.content.getSystemService
import cc.cryptopunks.crypton.context.*
import cc.cryptopunks.crypton.interactor.DisconnectAccountsInteractor
import cc.cryptopunks.crypton.interactor.ReconnectAccountsInteractor
import cc.cryptopunks.crypton.manager.PresenceManager
import cc.cryptopunks.crypton.manager.SessionManager
import cc.cryptopunks.crypton.notification.ShowMessageNotification
import cc.cryptopunks.crypton.presentation.PresentationManager
import cc.cryptopunks.crypton.selector.CurrentSessionSelector
import cc.cryptopunks.crypton.sys.*
import cc.cryptopunks.crypton.util.BroadcastError
import cc.cryptopunks.crypton.util.IOExecutor
import cc.cryptopunks.crypton.util.MainExecutor
import dagger.Binds
import dagger.Component
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [
        Application::class,
        Repo::class
    ], modules = [
        AndroidCore.Module::class,
        AndroidCore.Bindings::class
    ]
)
interface AndroidCore :
    Api.Core,
    SessionManager.Core,
    PresentationManager.Core,
    PresenceManager.Core {

    val application: Application
    val mainActivityClass: Class<*>

    val notificationManager: NotificationManager
    val connectivityManager: ConnectivityManager

    val currentSession: CurrentSessionSelector
    val reconnectAccounts: ReconnectAccountsInteractor
    val disconnectAccounts: DisconnectAccountsInteractor


    @dagger.Module
    class Module(
        @get:Provides val mainActivityClass: Class<*>,
        @get:Provides val createConnection: Connection.Factory
    ) {
        @get:Provides
        val serviceScope = Service.Scope()

        @get:Provides
        val mainExecutor = MainExecutor(ArchTaskExecutor.getMainThreadExecutor())

        @get:Provides
        val ioExecutor = IOExecutor(ArchTaskExecutor.getIOThreadExecutor())

        @get:Provides
        val broadcastError = BroadcastError()

        @Provides
        fun Application.notificationManager(): NotificationManager = getSystemService()!!

        @Provides
        fun Application.connectivityManager(): ConnectivityManager = getSystemService()!!

        @Provides
        fun Application.clipboardManager(): ClipboardManager = getSystemService()!!
    }

    @dagger.Module
    interface Bindings {
        @Binds
        fun GetNetworkStatus.getStatus(): Network.Sys.GetStatus

        @Binds
        fun SetToClipboard.setClip(): Clipboard.Sys.SetClip

        @Binds
        fun ShowMessageNotification.bind(): Message.Sys.ShowNotification

        @Binds
        fun StartIndicatorService.start(): Indicator.Sys.Show

        @Binds
        fun StopIndicatorService.stop(): Indicator.Sys.Hide
    }
}