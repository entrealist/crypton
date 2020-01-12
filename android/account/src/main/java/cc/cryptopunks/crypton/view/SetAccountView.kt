package cc.cryptopunks.crypton.view

import android.content.Context
import android.view.View
import cc.cryptopunks.crypton.account.R
import cc.cryptopunks.crypton.context.Service
import cc.cryptopunks.crypton.util.bindings.clicks
import cc.cryptopunks.crypton.util.ext.map
import cc.cryptopunks.crypton.viewmodel.SetAccountService.Input.AddAccount
import cc.cryptopunks.crypton.viewmodel.SetAccountService.Input.RegisterAccount
import cc.cryptopunks.crypton.widget.ServiceLayout
import kotlinx.android.synthetic.main.set_account.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SetAccountView(
    context: Context
) :
    ServiceLayout(context) {

    init {
        View.inflate(context, R.layout.set_account, this)
    }

    override fun Service.Binding.bind(): Job = launch {
        flowOf(
            addButton.clicks().map { AddAccount },
            registerButton.clicks().map { RegisterAccount }
        )
            .flattenMerge()
            .collect(output)
    }
}