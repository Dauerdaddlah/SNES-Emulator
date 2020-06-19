package de.dde.snes.processor

open class Register(var value: Int = 0) {
    open fun reset() {
        set(0)
    }

    fun inc() {
        set(value + 1)
    }

    fun dec() {
        set(value - 1)
    }

    fun set(value: Int) {
        this.value = checkValue(value)
    }

    open fun get(): Int = value

    open fun checkValue(value: Int): Int {
        return value
    }

    open val negative get() = value < 0
    open val zero get() = get() == 0
    val inv get() = value.inv()

    infix fun or(value: Int) = get() or value
    infix fun and(value: Int) = get() and value
    infix fun xor(value: Int) = get() xor value
}
open class Register8Bit : Register(0) {
    override fun checkValue(value: Int): Int {
        return value and 0xFF
    }

    override val negative get() = value and 0x80 != 0
}
open class Register16Bit : Register(0) {
    override fun checkValue(value: Int): Int {
        return value and 0xFFFF
    }

    override val negative get() = value and 0x8000 != 0
}

abstract class DiffSizeRegister : Register16Bit() {
    var _8bitMode: Boolean = false
        protected set

    fun checkBitMode() {
        _8bitMode = shallBe8Bit()
        set(value)
    }

    abstract fun shallBe8Bit(): Boolean

    override fun reset() {
        super.reset()
        checkBitMode()
        set(0)
    }

    override fun checkValue(value: Int): Int {
        return if (_8bitMode) (value and 0xFF) else super.checkValue(value)
    }

    override fun get() = if (_8bitMode) this.value and 0xFF else super.get()

    override val negative get() = negative(value)
    fun negative(value: Int) = if (_8bitMode) value and 0x80 != 0 else value and 0x8000 != 0
    override val zero get() = zero(value)
    fun zero(value: Int) = valueOf(value) == 0
    val overflow get() = overflow(value)
    fun overflow(value: Int) = if (_8bitMode) value and 0x40 != 0 else value and 0x4000 != 0

    fun valueOf(value: Int) = if (_8bitMode) value and 0xFF else value and 0xFFFF
}