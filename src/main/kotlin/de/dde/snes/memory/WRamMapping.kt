package de.dde.snes.memory

class WRamMapping : MemoryMappingArray() {
    override fun array(memory: Memory): ByteArray {
        return memory.wram
    }

    override fun index(bank: Bank, address: ShortAddress): Int {
        return if (bank == 0xFF) Memory.BANK_SIZE + address.shortAddress else address.shortAddress
    }
}