package de.dde.snes.processor.register

import de.dde.snes.asByte
import de.dde.snes.isBitSet

class StackPointer : Register8Bit16Bit(r8 = StackPointer8Bit()) {
    private class StackPointer8Bit : Register16Bit() {
        override val size: Int
            get() = 1

        override fun set(value: Int) {
            super.set(0x0100 or value.asByte())
        }

        override fun isNegative(value: Int): Boolean {
            return value.isBitSet(0x80)
        }

        override fun trimValue(value: Int): Int {
            return value.asByte()
        }
    }
}