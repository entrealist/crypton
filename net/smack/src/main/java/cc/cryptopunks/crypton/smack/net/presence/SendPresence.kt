package cc.cryptopunks.crypton.smack.net.presence

import cc.cryptopunks.crypton.context.Presence
import cc.cryptopunks.crypton.smack.util.SmackPresence
import org.jivesoftware.smack.XMPPConnection

class SendPresence(
    connection: XMPPConnection
) : Presence.Net.Send, (Presence) -> Unit by { presence ->
    connection.sendStanza(
        SmackPresence(
            org.jivesoftware.smack.packet.Presence.Type.fromString(
                presence.status.name.toLowerCase()
            )
        )
    )
}