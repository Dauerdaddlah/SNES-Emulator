package de.dde.snes.ppu

import de.dde.snes.Short
import de.dde.snes.highByte
import de.dde.snes.lowByte

class CGRAM {
    val colors = Array(0x100) {Color(0)}

    var address = 0

    private var high = false
    private var tempColor = 0

    fun reset() {
        address = 0
        high = false
        tempColor = 0
    }

    fun write(color: Int) {
        if (high) {
            colors[address].value = Short(tempColor, color)
            address++
        } else {
            tempColor = color
        }

        high = !high
    }

    fun read(): Int {
        val r = if (high) {
            colors[address].value.highByte()
            address++
        } else {
            colors[address].value.lowByte()
        }

        high = !high
        return r
    }
}

data class Color(var value: Int)