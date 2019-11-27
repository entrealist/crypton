package cc.cryptopunks.crypton.component

import cc.cryptopunks.crypton.navigation.service.OptionItemNavigationService
import cc.cryptopunks.crypton.presenter.AccountListPresenter
import cc.cryptopunks.crypton.viewmodel.AccountItemViewModel
import cc.cryptopunks.crypton.viewmodel.SetAccountViewModel
import cc.cryptopunks.crypton.viewmodel.SignInViewModel
import cc.cryptopunks.crypton.viewmodel.SignUpViewModel
import javax.inject.Provider

interface AccountPresentationComponent {
    val setAccountViewModel: SetAccountViewModel
    val signInViewModel: SignInViewModel
    val signUpViewModel: SignUpViewModel
    val accountListPresenter: AccountListPresenter
    val accountItemViewModelProvider: Provider<AccountItemViewModel>
    val navigationService: OptionItemNavigationService
}