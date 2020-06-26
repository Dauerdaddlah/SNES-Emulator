package de.dde.snes.memory

class CpuMapping : MemoryMapping {
    override fun readByte(memory: Memory, bank: Bank, address: ShortAddress): Int {
        error("cpu-mapping missing")
    }

    override fun writeByte(memory: Memory, bank: Bank, address: ShortAddress, value: Int) {
        error("cpu-mapping missing")
    }
}