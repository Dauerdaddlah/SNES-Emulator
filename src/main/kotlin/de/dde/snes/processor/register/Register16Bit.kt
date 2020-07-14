package de.dde.snes.processor.register

import de.dde.snes.asShort
import de.dde.snes.isBitSet

open class Register16Bit : RegisterBase() {
    override val size get() = 2

    override fun trimValue(value: Int): Int {
        return value.asShort()
    }

    override fun isNegative(value: Int): Boolean {
        return value.isBitSet(0x8000)
    }

}