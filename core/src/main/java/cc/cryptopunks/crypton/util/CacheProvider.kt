package cc.cryptopunks.crypton.util

import cc.cryptopunks.kache.core.Kache
import cc.cryptopunks.kache.core.KacheManager
import cc.cryptopunks.crypton.module.FeatureScope
import cc.cryptopunks.crypton.module.ViewModelScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationCacheProvider @Inject constructor(): Kache.Provider by KacheManager()

@FeatureScope
class GraphCacheProvider @Inject constructor(): Kache.Provider by KacheManager()

@ViewModelScope
class ViewModelProvider @Inject constructor(): Kache.Provider by KacheManager()