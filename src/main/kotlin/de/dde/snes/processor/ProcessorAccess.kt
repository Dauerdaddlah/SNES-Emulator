package de.dde.snes.processor

import de.dde.snes.Short
import de.dde.snes.highByte
import de.dde.snes.memory.Bank
import de.dde.snes.memory.ShortAddress
import de.dde.snes.memory.bank
import de.dde.snes.memory.shortAddress
import de.dde.snes.processor.addressmode.AddressModeResult
import de.dde.snes.withLongByte

interface ProcessorAccess {
    val mode: ProcessorMode

    fun fetch(): Int
    fun fetchShort(): Int
        = Short(fetch(), fetch())
    fun fetchLong(): Int
        = fetchShort().withLongByte(fetch())

    fun read(bank: Bank, address: ShortAddress): Int
    fun readShort(bank: Bank, address: ShortAddress): Int
        = Short(read(bank, address), read(bank, address + 1))
    fun readLong(bank: Bank, address: ShortAddress): Int
        = readShort(bank, address).withLongByte(read(bank, address + 2))

    fun getBankFor(result: AddressModeResult): Bank
}