package de.dde.snes.memory

import java.util.*

interface MemoryMapping {
    fun readByte(bank: Bank, address: ShortAddress): Int
    fun writeByte(bank: Bank, address: ShortAddress, value: Int)
}

abstract class MemoryMappingArray(
    private val array: ByteArray
) : MemoryMapping {
    override fun readByte(bank: Bank, address: ShortAddress): Int {
        var i = index(bank, address)
        i = i.rem(array.size)
        return array[i].toInt() and 0xFF
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        var i = index(bank, address)
        i = i.rem(array.size)
        array[i] = value.toByte()
    }

    abstract fun index(bank: Bank, address: ShortAddress): Int
}
