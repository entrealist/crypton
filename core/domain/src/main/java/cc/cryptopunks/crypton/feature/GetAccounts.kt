package cc.cryptopunks.crypton.feature

import cc.cryptopunks.crypton.context.Get
import cc.cryptopunks.crypton.feature

internal fun getAccounts() = feature(
    handler = { out, _: Get.Accounts ->
        out(accounts.get())
    }
)