package de.dde.snes

import de.dde.snes.apu.APU
import de.dde.snes.memory.Memory
import de.dde.snes.processor.Processor

class SNES {
    val memory = Memory(this)
    val processor = Processor(memory)
    val apu = APU()

    var cartridge: Cartridge? = null
        private set

    fun reset() {
        memory.reset()
        processor.reset()
        apu.reset()
    }

    fun insertCartridge(cartridge: Cartridge) {
        this.cartridge = cartridge

        memory.initializeFor(cartridge)
    }
}