package de.dde.snes

import de.dde.snes.apu.APU
import de.dde.snes.cartridge.Cartridge
import de.dde.snes.controller.Controllers
import de.dde.snes.memory.MemoryImpl
import de.dde.snes.ppu.PPU
import de.dde.snes.processor.Processor

class SNES {
    val memory = MemoryImpl(this)
    val processor = Processor(memory)
    val apu = APU()
    val dma = Array(8) { DMA(this, it) }
    val ppu = PPU(this)
    val controllers = Controllers(this)

    var version = Version.PAL

    var cartridge: Cartridge? = null
        private set

    fun reset() {
        memory.reset()
        processor.reset()
        apu.reset()
        ppu.reset()
        controllers.reset()
    }

    fun insertCartridge(cartridge: Cartridge) {
        this.cartridge = cartridge

        memory.initializeFor(cartridge)
    }

    fun start() {
        try {
            while (true) {
                if (processor.instructionCount.rem(1000000) == 0L) {
                    slog("${processor.instructionCount / 1000000}")
                }
                step()
            }
        } catch (e: Exception) {
            slog("AFTER ${processor.instructionCount} - ${processor.cycles}")
            e.printStackTrace()
        }
    }

    private fun step() {
        val cyclesStart = processor.cycles

        processor.executeNextInstruction()
        ppu.updateCycles(processor.cycles, processor.cycles - cyclesStart)
    }

    enum class Version(
        val hz: Int,
        /** screen resolution width */
        val width: Int,
        /** screen resolution height */
        val heigth: Int,
        /** virtual resolution width after the end of the visible screen to simulate the hblank */
        val vWidthEnd: Int,
        /** virtual resolution height after the end of the visible screen to simulate the vblank */
        val vHeightEnd: Int
    ) {
        PAL(50, 256, 239, 312 - 256, 340 - 256), // 50Hz  | 239 x 256 | 478 x 512 | 312 x 340
        NTSC(60, 256, 224, 262 - 224, 340 - 256); // 60 Hz | 224 x 256 | 448 x 512 | 262 x 340

        // first scanline is 1 (0-based)
        // horizontal position is 22 to 277 (inclusive) (0-based)
    }
}