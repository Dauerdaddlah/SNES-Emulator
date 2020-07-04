package de.dde.snes

import de.dde.snes.apu.APU
import de.dde.snes.cartridge.Cartridge
import de.dde.snes.cpu.CPU
import de.dde.snes.memory.Memory
import de.dde.snes.ppu.PPU
import de.dde.snes.processor.Processor

class SNES {
    val memory = Memory(this)
    val processor = Processor(memory)
    val apu = APU()
    val cpu = CPU()
    val dma = Array(8) { DMA(it) }
    val ppu = PPU()

    var cartridge: Cartridge? = null
        private set

    fun reset() {
        memory.reset()
        processor.reset()
        apu.reset()
        cpu.reset()
        ppu.reset()
    }

    fun insertCartridge(cartridge: Cartridge) {
        this.cartridge = cartridge

        memory.initializeFor(cartridge)
    }
}