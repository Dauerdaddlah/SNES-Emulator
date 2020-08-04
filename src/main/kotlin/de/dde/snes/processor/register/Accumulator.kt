package de.dde.snes.processor.register

import de.dde.snes.Short
import de.dde.snes.highByte
import de.dde.snes.lowByte

class Accumulator : Register8Bit16Bit() {
    override fun updateR(oldR: Register, newR: Register) {
        if(oldR.size == 2) {
            super.updateR(oldR, newR)
        } else {
            ensureR16()
        }
    }

    fun getFull(): Int {
        ensureR16()
        return r16.get()
    }

    fun setFull(value: Int) {
        r16.set(value)

        if (!size16Bit) {
            r8.set(value)
        }
    }

    fun xba() {
        if (size16Bit) {
            val t = r16.get()
            r16.set(Short(t.highByte(), t.lowByte()))
        } else {
            r16.set(Short(r16.get().highByte(), r8.get()))
            r8.set(r16.get())
        }
    }

    private fun ensureR16() {
        if (!size16Bit) {
            r16.set(Short(r8.get(), r16.get().highByte()))
        }
    }
}