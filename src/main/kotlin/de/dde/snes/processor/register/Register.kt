package de.dde.snes.processor.register

interface Register {
    val size: Int
    val negative: Boolean

    fun reset()
    fun set(value: Int)
    fun get(): Int

    fun trimValue(value: Int): Int
    fun isNegative(value: Int): Boolean
    fun isZero(value: Int): Boolean

    fun inc() {
        set(get() + 1)
    }

    fun dec() {
        set(get() - 1)
    }

    val zero get() = get() == 0
}

abstract class RegisterBase(var value: Int = 0) : Register {
    override val negative get() = isNegative(value)

    override fun reset() {
        value = 0
    }

    override fun set(value: Int) {
        this.value = trimValue(value)
    }

    override fun get(): Int {
        return value
    }

    override fun isZero(value: Int): Boolean {
        return trimValue(value) == 0
    }
}