package cc.cryptopunks.crypton.interactor

import cc.cryptopunks.crypton.Connectable
import cc.cryptopunks.crypton.context.Address
import cc.cryptopunks.crypton.context.Message
import cc.cryptopunks.crypton.context.Notification
import cc.cryptopunks.crypton.context.SessionScope
import cc.cryptopunks.crypton.top


fun updateChatNotification(): SessionScope.(List<Message>) -> Unit {
    var current = emptyList<Message>()
    return { messages ->

        current.minus(messages).asNotifications(account.address, navigateChatId.value).forEach {
            notificationSys.cancel(it)
        }

        current = connectableBindingsStore.consume(messages)

        current.asNotifications(account.address, navigateChatId.value).forEach {
            notificationSys.show(it)
        }
    }
}

private fun List<Message>.asNotifications(
    account: Address,
    destination: Int
) = groupBy { message: Message ->
    message.chat
}.map { (address: Address, messages: List<Message>) ->
    Notification.Messages(
        chatAddress = address,
        messages = messages,
        account = account,
        destination = destination
    )
}

private fun Connectable.Binding.Store.consume(messages: List<Message>) = top()
    ?.services
    ?.filterIsInstance<Message.Consumer>()
    ?.let { consumers ->
        messages.filterNot { message ->
            consumers.any { consumer ->
                consumer.canConsume(message)
            }
        }
    }
    ?: messages
