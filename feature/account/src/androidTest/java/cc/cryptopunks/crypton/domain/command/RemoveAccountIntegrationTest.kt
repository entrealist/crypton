package cc.cryptopunks.crypton.domain.command

import cc.cryptopunks.crypton.IntegrationTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoveAccountIntegrationTest : IntegrationTest() {

    override fun setUp(): Unit = with(client1) {
        insertAccount()
    }

    @Test
    fun invoke(): Unit = with(component) {
        // given
        val id = 1L
        val expected = null

        // when
        val connection = removeAccount(id).test()

        // then
        connection
            .assertComplete()
            .assertNoErrors()

        assertEquals(
            expected,
            accountDao.contains(id)
        )

        assertNull(
            clientCache[id]
        )
    }
}