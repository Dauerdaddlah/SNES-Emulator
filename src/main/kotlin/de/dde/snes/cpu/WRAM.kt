package de.dde.snes.cpu

import de.dde.snes.asByte
import de.dde.snes.memory.Memory

class WRAM {
    val wram: ByteArray = ByteArray(128 * Memory._1K)

    var address = 0

    fun reset() {
        wram.fill(0)
    }

    fun read(): Int {
        val r = wram[address].toInt().asByte()
        address++
        return r
    }

    fun write(value: Int) {
        wram[address] = value.toByte()
        address++
    }
}