package de.dde.snes.memory

import de.dde.snes.cartridge.Cartridge

interface Memory {
    fun reset()
    fun initializeFor(cartridge: Cartridge)

    fun readByte(bank: Bank, address: ShortAddress): Int
    fun writeByte(bank: Bank, address: ShortAddress, value: Int)

    companion object {
        const val _1K = 0x400
        const val _1M = _1K * _1K

        const val ADDRESS_SPACE = 0x1000000
        const val PAGE_SIZE = 0x1000
        const val BANK_SIZE = 0x10000

        const val OPEN_BUS = -1
    }
}

typealias Bank = Int
typealias ShortAddress = Int
typealias FullAddress = Int

fun bank(bankNo: Int): Bank
    = bankNo and 0xFF
fun shortAddress(shortAddress: Int): ShortAddress
    = shortAddress and 0xFFFF
fun fullAddress(bank: Bank, address: ShortAddress)
    = (bank shl 16) or address
fun fullAddress(fullAddress: Int): FullAddress
    = fullAddress and 0xFFFFFF

val FullAddress.bank get() = (this shr 16) and 0xFF
val FullAddress.shortAddress get() = this and 0xFFFF