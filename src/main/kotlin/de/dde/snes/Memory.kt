package de.dde.snes

import java.nio.ByteBuffer

class Memory(
    val cartridge: Cartridge,
    val wram: ByteBuffer = ByteBuffer.allocate(_1K * 128), // 128 k
    val sram: ByteBuffer = ByteBuffer.allocate(_1K * cartridge.header.sramSizeKb.toInt()) // max 512 k
) {
    /** Memory Data Register - holds the last value read or written. Every time, an invalid address is requested, this value will be used */
    private var mdr: Byte = 0
    // 128 k wram

    fun read(bank: BankNo, address: ShortAddress, byte: Boolean) = if (byte) readByte(bank, address) else readShort(bank, address)
    fun read(address: FullAddress, byte: Boolean) = if (byte) readByte(address.bankNo, address.address) else readShort(address.bankNo, address.address)

    fun readByte(bank: BankNo, address: ShortAddress): Int {
        // TODO
        error("not implemented yet")
    }

    fun readShort(bank: BankNo, address: ShortAddress): Int {
        return readByte(bank, address) + (readByte(bank, address + 1) shl 8)
    }

    fun readAddress(bank: BankNo, address: ShortAddress): Int {
        return readShort(bank, address) + (readByte(bank, address + 2) shl 16)
    }

    fun writeByte(bank: BankNo, address: ShortAddress, value: Int) {

    }

    fun writeShort(bank: BankNo, address: ShortAddress, value: Int) {
        writeByte(bank, address, address and 0xFF)
        writeByte(bank, ShortAddress(address + 1), value and 0xFF00 shr 8)
    }

    companion object {
        private const val _1K = 0x400
        private const val _1M = _1K * _1K
    }
}

typealias BankNo = Int
typealias ShortAddress = Int
typealias FullAddress = Int

val FullAddress.bankNo: BankNo get() = this ushr 16 and 0xFF
val FullAddress.address: ShortAddress get() = this and 0xFFFF

fun ShortAddress(address: Int): ShortAddress = address and 0xFFFF
fun FullAddress(bank: BankNo, address: ShortAddress): FullAddress = (bank shl 16) + (address and 0xFF)