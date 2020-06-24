package de.dde.snes.memory

class PpuMapping : MemoryMapping {
    override fun readByte(memory: Memory, bank: BankNo, address: ShortAddress): Int {
        error("ppu mapping missing")
    }

    override fun writeByte(memory: Memory, bank: BankNo, address: ShortAddress, value: Int) {
        error("ppu mapping missing")
    }
}