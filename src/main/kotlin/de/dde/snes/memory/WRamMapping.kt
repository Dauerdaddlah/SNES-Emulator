package de.dde.snes.memory

class WRamMapping(
    wram: ByteArray
) : MemoryMappingArray(wram) {
    override fun index(bank: Bank, address: ShortAddress): Int {
        return if (bank == 0xFF) Memory.BANK_SIZE + address.shortAddress else address.shortAddress
    }
}