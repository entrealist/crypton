package cc.cryptopunks.crypton.util

open class State<T>(
    initial: T
) {
    private var value: T = initial
    operator fun invoke(reduce: T.() -> T) = value.reduce().also { value = it }
    fun get() = value
}