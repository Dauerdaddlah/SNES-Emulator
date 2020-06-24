package de.dde.snes.memory

interface MemoryMapping {
    fun readByte(memory: Memory, bank: BankNo, address: ShortAddress): Int
    fun writeByte(memory: Memory, bank: BankNo, address: ShortAddress, value: Int)
}

abstract class MemoryMappingArray : MemoryMapping {
    override fun readByte(memory: Memory, bank: BankNo, address: ShortAddress): Int {
        val i = index(bank, address)
        val a = array(memory)
        return a[i.rem(a.size)].toInt()
    }

    override fun writeByte(memory: Memory, bank: BankNo, address: ShortAddress, value: Int) {
        array(memory)[index(bank, address)] = value.toByte()
    }

    abstract fun array(memory: Memory): ByteArray
    abstract fun index(bank: BankNo, address: ShortAddress): Int
}
