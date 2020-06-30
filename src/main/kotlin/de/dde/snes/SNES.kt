package de.dde.snes

import de.dde.snes.apu.APU
import de.dde.snes.memory.Memory
import de.dde.snes.processor.Processor

class SNES(
    val cartridge: Cartridge
) {
    val memory = Memory(this)
    val processor = Processor(memory)
    val apu = APU()

}