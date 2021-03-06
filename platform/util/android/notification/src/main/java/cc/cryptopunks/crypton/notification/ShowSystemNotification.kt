package cc.cryptopunks.crypton.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N_MR1

class ShowForegroundNotification(
    private val service: Service,
    private val notificationManager: NotificationManager
) : (Int, Notification) -> Unit by { id, notification ->

    service.startForeground(id, notification)
    when {
        SDK_INT > N_MR1 -> service.startForeground(id, notification)
        else -> notificationManager.notify(id, notification)
    }
}
