package de.dde.snes.memory

import de.dde.snes.SNES

interface MemoryMapping {
    fun readByte(bank: Bank, address: ShortAddress): Int
    fun writeByte(bank: Bank, address: ShortAddress, value: Int)
}

abstract class MemoryMappingArray(
    private val array: ByteArray
) : MemoryMapping {
    override fun readByte(bank: Bank, address: ShortAddress): Int {
        val i = index(bank, address)
        return array[i.rem(array.size)].toInt()
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        array[index(bank, address)] = value.toByte()
    }

    abstract fun index(bank: Bank, address: ShortAddress): Int
}
