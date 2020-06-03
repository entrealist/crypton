package cc.cryptopunks.crypton.sys

import android.app.Application
import androidx.core.content.getSystemService
import cc.cryptopunks.crypton.context.*
import kotlin.reflect.KClass

class AndroidSys(
    application: Application,
    notificationFactories: Map<KClass<out Notification>, (Notification) -> android.app.Notification>
) : Sys {

    override val routeSys: Route.Sys by lazy {
        RouteSys()
    }

    override val indicatorSys: Indicator.Sys by lazy {
        IndicatorSys(application)
    }

    override val notificationSys: Notification.Sys by lazy {
        NotificationSys(
            notificationManager = application.getSystemService()!!,
            notificationFactories = notificationFactories
        )
    }

    override val clipboardSys: Clip.Board.Sys by lazy {
        ClipBoardSys(
            clipboard = application.getSystemService()!!
        )
    }

    override val networkSys: Network.Sys by lazy {
        NetworkSys(
            scope = Service.Scope(),
            connectivityManager = application.getSystemService()!!
        )
    }

    override fun createRouteSys(): Route.Sys = RouteSys()
}