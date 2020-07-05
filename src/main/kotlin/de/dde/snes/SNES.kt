package de.dde.snes

import de.dde.snes.apu.APU
import de.dde.snes.cartridge.Cartridge
import de.dde.snes.controller.Controllers
import de.dde.snes.cpu.CPU
import de.dde.snes.memory.Memory
import de.dde.snes.ppu.PPU
import de.dde.snes.processor.Processor

class SNES {
    val memory = Memory(this)
    val processor = Processor(this)
    val apu = APU()
    val cpu = CPU()
    val dma = Array(8) { DMA(this, it) }
    val ppu = PPU()
    val controllers = Controllers(this)

    var version = Version.PAL

    var cartridge: Cartridge? = null
        private set

    fun reset() {
        memory.reset()
        processor.reset()
        apu.reset()
        cpu.reset()
        ppu.reset()
        controllers.reset()
    }

    fun insertCartridge(cartridge: Cartridge) {
        this.cartridge = cartridge

        memory.initializeFor(cartridge)
    }

    enum class Version {
        PAL, // 50Hz  | 239 x 256 | 478 x 512
        NTSC // 60 Hz | 224 x 256 | 448 x 512
    }
}