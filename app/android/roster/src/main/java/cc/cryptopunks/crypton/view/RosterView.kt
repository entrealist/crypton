package cc.cryptopunks.crypton.view

import android.content.Context
import android.view.View
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cc.cryptopunks.crypton.adapter.RosterAdapter
import cc.cryptopunks.crypton.roster.R
import cc.cryptopunks.crypton.Connector
import cc.cryptopunks.crypton.context.Get
import cc.cryptopunks.crypton.context.Roster
import cc.cryptopunks.crypton.context.Subscribe
import cc.cryptopunks.crypton.navigate.navigateChat
import cc.cryptopunks.crypton.navigate.navigateCreateChat
import cc.cryptopunks.crypton.util.logger.typedLog
import cc.cryptopunks.crypton.widget.ActorLayout
import kotlinx.android.synthetic.main.roster.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

class RosterView(
    context: Context
) : ActorLayout(context) {

    private val rosterAdapter = RosterAdapter(coroutineContext)

    private val rosterItemDecorator = RosterItemDecorator(
        context = context,
        paddingLeft = resources.getDimensionPixelSize(R.dimen.roster_divider_padding_left),
        paddingRight = resources.getDimensionPixelSize(R.dimen.roster_divider_padding_right)
    )

    private val log = typedLog()

    init {
        log.builder.d { status = "Init" }
        View.inflate(context, R.layout.roster, this)
        rosterRecyclerView.apply {
            addItemDecoration(rosterItemDecorator)
            layoutManager = LinearLayoutManager(context)
            adapter = rosterAdapter
        }
        createConversationButton.setOnClickListener {
            findNavController().navigateCreateChat()
        }
    }

    override fun Connector.connect(): Job = launch {
        log.d { "Connect" }
        launch {
            input.filterIsInstance<Roster.Items>().collect {
                log.d { "Received ${it.list.size} items" }
                rosterAdapter.submitList(it.list)
            }
        }
        launch {
            Get.RosterItems.out()
            Subscribe.RosterItems(true).out()
        }
        launch {
            rosterAdapter.clicks.asFlow().collect {
                findNavController().navigateChat(
                    account = it.item.account,
                    chat = it.item.chatAddress
                )
            }
        }
    }

    override fun onDetachedFromWindow() {
        coroutineContext.cancelChildren()
        super.onDetachedFromWindow()
    }
}
