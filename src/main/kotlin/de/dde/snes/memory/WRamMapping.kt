package de.dde.snes.memory

class WRamMapping : MemoryMappingArray() {
    override fun array(memory: Memory): ByteArray {
        return memory.wram
    }

    override fun index(bank: BankNo, address: ShortAddress): Int {
        return if (bank.bankNo == 0xFF) Memory.BANK_SIZE + address.shortAddress else address.shortAddress
    }
}