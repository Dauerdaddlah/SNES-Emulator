package de.dde.snes.ppu

class CGRAM {
    var address = 0

    fun reset() {
        address = 0
    }

    fun write(color: Int) {
        address++
        // TODO
    }

    fun read(): Int {
        // TODO
        return -1
    }
}