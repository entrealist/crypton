package cc.cryptopunks.crypton.notification

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import cc.cryptopunks.crypton.chat.R
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Message
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private val List<Message>.notificationId get() = first().chatAddress.hashCode()
private val List<Message>.address get() = first().chatAddress
private val List<Message>.timestamp get() = last().timestamp

class NotifyUnreadMessages @Inject constructor(
    private val context: Application,
    private val notificationManager: NotificationManager,
    private val mainActivityClass: Class<*>
) : Message.Notify.Unread {

    private val person = Person.Builder().setName("Me").build()

    private val dateFormat = SimpleDateFormat(
        "d MMM • HH:mm",
        Locale.getDefault()
    )

    override fun invoke(messages: List<Message>) {
        messages.takeIf {
            it.isNotEmpty()
        }?.showNotification()
    }

    override fun cancel(messages: List<Message>) {
        messages
            .mapTo(mutableSetOf(), Message::chatAddress)
            .forEach { notificationManager.cancel(it.hashCode()) }
    }

    private fun List<Message>.showNotification() = notificationManager
        .notify(notificationId, createNotification())

    private fun List<Message>.createNotification() = NotificationCompat
        .Builder(context, Message.Notification.channelId)
        .setContentTitle(address)
        .setContentText(formatMessageCount())
        .setContentInfo(formatMessageCount())
        .setSubText(formatDate())
        .setWhen(timestamp)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentIntent(pendingIntent())
        .setGroup(address.id)
        .setGroupSummary(true)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setStyle(getMessageStyle())
        .build()

    private fun List<Message>.getMessageStyle() = NotificationCompat
        .MessagingStyle(person).also { style ->
            val senders = mutableMapOf<Address, Person>()
            fun Message.getSenderPerson() = senders.getOrPut(from.address) {
                Person.Builder()
                    .setName(from.address.local.capitalize())
                    .build()
            }
            style.isGroupConversation = false
            forEach { message ->
                style.addMessage(
                    message.text,
                    message.timestamp,
                    message.getSenderPerson()
                )
            }
        }

    private fun List<Message>.formatDate() = dateFormat
        .format(last().timestamp)

    private fun List<Message>.formatMessageCount() = context.resources
        .getQuantityText(R.plurals.messages_count, size)


    private fun pendingIntent() = PendingIntent.getActivity(context, 0, mainActivityIntent(), 0)

    private fun mainActivityIntent() = Intent(
        context,
        mainActivityClass
    )
}