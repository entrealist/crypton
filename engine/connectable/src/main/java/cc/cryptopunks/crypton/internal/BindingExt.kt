package cc.cryptopunks.crypton.internal

import cc.cryptopunks.crypton.Actor
import cc.cryptopunks.crypton.Connectable
import cc.cryptopunks.crypton.Connector
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

internal fun Binding.cancel() {
    actorContext.input.cancel()
    serviceContext.input.cancel()
}

internal suspend fun Binding.setActor(connectable: Actor?) = copy(
    actorContext = actorContext.set(connectable)
)

internal suspend fun Binding.setService(connectable: Connectable?) = copy(
    serviceContext = serviceContext.set(connectable)
)

private suspend fun Binding.Context.set(connectable: Connectable?): Binding.Context = this
    .apply { job?.cancel() }
    .copy(getConnector().connect(connectable))

private fun Binding.Context.copy(connectorJob: Pair<Connector, Job?>) = copy(
    connector = connectorJob.first,
    job = connectorJob.second
)

private fun Binding.Context.getConnector(): Connector =
    if (job == null)
        connector else
        createConnector(
            input.openSubscription(),
            output
        )

private fun Connector.connect(connectable: Connectable?): Pair<Connector, Job?> =
    this to connectable?.run { connect() }
