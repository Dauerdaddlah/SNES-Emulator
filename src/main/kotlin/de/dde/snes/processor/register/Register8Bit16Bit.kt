package de.dde.snes.processor.register

open class Register8Bit16Bit(
    protected val r8: Register = Register8Bit(),
    protected val r16: Register = Register16Bit()
) : Register {
    var size16Bit: Boolean
        get() = r === r16
        set(value) {
            val newR = if (value) r16 else r8

            if (newR !== r) {
                updateR(r, newR)
                r = newR
            }
        }

    private var r: Register = r8

    override val size get() =  r.size

    override val negative: Boolean
        get() = r.negative

    protected open fun updateR(oldR: Register, newR: Register) {
        newR.set(oldR.get())
    }

    override fun reset() {
        r.reset()
    }

    override fun set(value: Int) {
        r.set(value)
    }

    override fun get(): Int {
        return r.get()
    }

    override fun trimValue(value: Int): Int {
        return r.trimValue(value)
    }

    override fun isNegative(value: Int): Boolean {
        return r.isNegative(value)
    }

    override fun isZero(value: Int): Boolean {
        return r.isZero(value)
    }
}