package de.dde.snes.memory

class LoRomROMMapping(
    rom: ByteArray
) : MemoryMappingArray(rom) {
    override fun index(bank: Bank, address: ShortAddress): Int {
        // bank and 0x7f is the same as calculating bank - 0x80 if it is above 0x80 -> this way we handle alls mirrors the same
        // same logic for the address itself - and 0x7FFF is the same as calculating - 0x8000 if it is above 0x8000 which handle the upper and lower half the same
        return ((bank and 0x7F) * Memory.BANK_SIZE / 2) + (address and 0x7FFF)
    }
}

class LoRomRAMMapping(
    ram: ByteArray
) : MemoryMappingArray(ram) {
    override fun index(bank: Bank, address: ShortAddress): Int {
        // sram is in bank 0x70 - 0x7F and 0xF0 - 0xFF
        // therefore for the index only the lower nibble is needed
        return (bank and 0x0F * Memory.BANK_SIZE / 2) + address
    }
}
