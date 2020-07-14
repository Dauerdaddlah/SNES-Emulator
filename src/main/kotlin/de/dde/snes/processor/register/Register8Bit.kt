package de.dde.snes.processor.register

import de.dde.snes.asByte
import de.dde.snes.isBitSet

class Register8Bit: RegisterBase() {
    override val size get() = 1

    override fun trimValue(value: Int): Int {
        return value.asByte()
    }

    override fun isNegative(value: Int): Boolean {
        return value.isBitSet(0x80)
    }
}