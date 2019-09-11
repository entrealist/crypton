package cc.cryptopunks.crypton.domain.query

import cc.cryptopunks.crypton.entity.Account
import cc.cryptopunks.crypton.entity.Account.Status.Connected
import cc.cryptopunks.crypton.util.Schedulers
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.reactivex.processors.PublishProcessor
import org.junit.Before
import org.junit.Test

class NewConnectedAccountsUnitTest {

    @RelaxedMockK
    lateinit var dao: Account.Dao
    private lateinit var newAccountConnected: NewAccountConnected

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        newAccountConnected = NewAccountConnected(dao, Schedulers.currentThread)
    }

    @Test
    operator fun invoke() {
        // given
        val subject = PublishProcessor.create<List<Account>>()
        every { dao.flowableList() } returns subject

        // when
        val addedAccounts = newAccountConnected().test()

        subject.onNext(
            emptyList()
        )
        subject.onNext(
            listOf(
                Account(id = 1, status = Connected),
                Account(id = 2)
            )
        )
        subject.onNext(
            listOf(
                Account(id = 1, status = Connected),
                Account(id = 2, status = Connected)
            )
        )
        subject.onNext(
            listOf(
                Account(id = 1)
            )
        )

        // then
        addedAccounts
            .assertNoErrors()
            .assertValues(1, 2)
    }
}