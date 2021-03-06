package cc.cryptopunks.crypton.handler

import cc.cryptopunks.crypton.context.Chat
import cc.cryptopunks.crypton.context.Exec
import cc.cryptopunks.crypton.context.Presence
import cc.cryptopunks.crypton.context.Resource
import cc.cryptopunks.crypton.context.Roster
import cc.cryptopunks.crypton.handle
import cc.cryptopunks.crypton.interactor.createChat
import cc.cryptopunks.crypton.interactor.storePresence
import cc.cryptopunks.crypton.util.logger.log

internal fun handlePresence() = handle { _, (presence): Exec.HandlePresence ->
    storePresence(presence)

    if (presence.resource != Resource.Empty)
        if (presence.resource.address != address)
            if (chatRepo.contains(presence.resource.address).not()) {
                log.d { "Creating chat from presence ${presence.resource}" }
                createChat(
                    Chat(
                        address = presence.resource.address,
                        account = address
                    )
                )
            }

    when (presence.status) {

        Presence.Status.Unsubscribed -> {
            subscriptions reduce { minus(presence.resource.address) }
            Unit
        }

        Presence.Status.Subscribe -> {
            when (subscriptionStatus(presence.resource.address)) {
                Roster.Item.Status.none -> {
                    log.d { "Received subscription request from ${presence.resource}" }
                    log.d { "Auto accepting feedback subscription from ${presence.resource}" }
                    sendPresence(
                        Presence(
                            resource = presence.resource,
                            status = Presence.Status.Subscribed
                        )
                    )
                    subscribe(presence.resource.address)
                    createChat(
                        Chat(
                            address = presence.resource.address,
                            account = address
                        )
                    )
                }
                Roster.Item.Status.from -> {
                    subscribe(presence.resource.address)
                    createChat(
                        Chat(
                            address = presence.resource.address,
                            account = address
                        )
                    )
                }
                Roster.Item.Status.to -> {
                    log.d { "Auto accepting feedback subscription from ${presence.resource}" }
                    sendPresence(
                        Presence(
                            resource = presence.resource,
                            status = Presence.Status.Subscribed
                        )
                    )
                }
                Roster.Item.Status.both,
                Roster.Item.Status.remove -> Unit
            }
        }

        Presence.Status.Subscribed,
        Presence.Status.Unavailable,
        Presence.Status.Available,
        Presence.Status.Unsubscribe,
        Presence.Status.Error,
        Presence.Status.Probe -> Unit
    }
    Unit
}
