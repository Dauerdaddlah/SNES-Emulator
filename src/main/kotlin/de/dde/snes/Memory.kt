package de.dde.snes

import java.nio.ByteBuffer

class Memory(
    val cartridge: Cartridge,
    val wram: ByteArray = ByteArray(_1K * 128), // 128 k
    val sram: ByteArray = ByteArray(_1K * cartridge.header.sramSizeKb.toInt()) // max 512 k
) {
    /** Memory Data Register - holds the last value read or written. Every time, an invalid address is requested, this value will be used */
    private var mdr: Byte = 0

    fun read(bank: BankNo, address: ShortAddress, byte: Boolean) = if (byte) readByte(bank, address) else readShort(bank, address)
    fun read(address: FullAddress, byte: Boolean) = if (byte) readByte(address.bankNo, address.shortAaddress) else readShort(address.bankNo, address.shortAaddress)

    fun readByte(bank: BankNo, address: ShortAddress): Int {
        val b = cartridge.mode.byte(this, bank.bankNo, address.shortAddress)

        if (b != (-1).toByte()) {
            mdr = b
        }

        return mdr.toInt() and 0xFF
    }

    fun readShort(bank: BankNo, address: ShortAddress): Int {
        return readByte(bank, address) + (readByte(bank, shortAddress(address.shortAddress + 1)) shl 8)
    }

    fun readAddress(bank: BankNo, address: ShortAddress): Int {
        return readShort(bank, address) + (readByte(bank, shortAddress(address.shortAddress + 2)) shl 16)
    }

    fun readHardwareRegister(address: Int) {

    }

    fun writeByte(bank: BankNo, address: ShortAddress, value: Int) {

    }

    fun writeShort(bank: BankNo, address: ShortAddress, value: Int) {
        writeByte(bank, address, value and 0xFF)
        writeByte(bank, ShortAddress(address.shortAddress + 1), value and 0xFF00 shr 8)
    }

    companion object {
        private const val _1K = 0x400
        private const val _1M = _1K * _1K
    }
}

inline fun bankNo(bankNo: Int): BankNo = BankNo(bankNo and 0xFF)
inline fun shortAddress(shortAddress: Int): ShortAddress = ShortAddress(shortAddress and 0xFFFF)
inline fun fullAddress(fullAddress: Int): FullAddress = FullAddress(fullAddress and 0xFFFFFF)

inline class BankNo(val bankNo: Int)

inline class ShortAddress(val shortAddress: Int)

inline class FullAddress(val fullAddress: Int) {

    constructor(bankNo: BankNo, shortAddress: ShortAddress) : this((bankNo.bankNo shl 16) + (shortAddress.shortAddress))

    val bankNo: BankNo get() = bankNo(fullAddress ushr 16)
    val shortAaddress: ShortAddress get() = shortAddress(fullAddress)
}