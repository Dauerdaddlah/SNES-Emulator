package de.dde.snes.memory

class HiRomROMMapping(
    rom: ByteArray
) : MemoryMappingArray(rom) {
    override fun index(bank: Bank, address: ShortAddress): Int {
        // hirom begins from bank 0xC0 to 0xFF from address 0x0000 to 0xFFFF
        // bank 0x80 onwards beginning at address 0x8000 mirror the rom 0x40 banks later
        // bank 0x00 to 0x3F address >= 0x8000 and banks 0x40 onwards mirror the banks 0x80 later
        // so for calculating the bank for the index the highest 2 bits of the bank are unnecessary
        return (bank and 0x3F * Memory.BANK_SIZE) + address
    }
}

class HiRomRAMMapping(
    ram: ByteArray
) : MemoryMappingArray(ram) {
    override fun index(bank: Bank, address: ShortAddress): Int {
        // sram is from bank 0x00/10/30/40/80/90/A0/B0 to bank 0x0F..BF always from address 0x600 to 0x7FFF
        // so for the index only the lower nibble of the bank is needed
        return (bank and 0xF * 0x2000) + (address - 0x6000)
    }
}