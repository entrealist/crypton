package cc.cryptopunks.crypton.smack.net.account

import cc.cryptopunks.crypton.context.Account
import org.jivesoftware.smack.tcp.XMPPTCPConnection

class IsAccountAuthenticated(
    connection: XMPPTCPConnection
) : Account.Net.IsAuthenticated, () -> Boolean by {
    connection.isAuthenticated
}