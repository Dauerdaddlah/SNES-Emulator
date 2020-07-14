package de.dde.snes.processor.register

import de.dde.snes.Short
import de.dde.snes.highByte
import de.dde.snes.lowByte

class Accumulator : Register8Bit16Bit() {
    fun xba() {
        if (size16Bit) {
            val t = r16.get()
            r16.set(Short(t.highByte(), t.lowByte()))
        } else {
            r16.set(Short(r16.get().highByte(), r8.get()))
            r8.set(r16.get())
        }
    }
}