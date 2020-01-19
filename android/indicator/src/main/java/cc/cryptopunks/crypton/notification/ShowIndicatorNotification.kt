package cc.cryptopunks.crypton.notification

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import cc.cryptopunks.crypton.context.Indicator
import cc.cryptopunks.crypton.indicator.R
import javax.inject.Inject

class ShowIndicatorNotification @Inject constructor(
    private val showNotification: ShowForegroundNotification,
    private val context: Application,
    private val mainActivityClass: Class<*>
) : () -> Unit {

    override fun invoke() = showNotification(
        Indicator.Notification.id,
        createNotification()
    )

    private fun createNotification(): Notification = NotificationCompat
        .Builder(context, Indicator.Notification.channelId)
        .setContentTitle(context.getText(R.string.app_name))
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentIntent(pendingIntent())
        .setOngoing(true)
        .setShowWhen(false)
        .build()


    private fun pendingIntent() = PendingIntent
        .getActivity(context, 0, mainActivityIntent(), 0)

    private fun mainActivityIntent() = Intent(
        context,
        mainActivityClass
    )
}