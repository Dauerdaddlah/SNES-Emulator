package de.dde.snes.cartridge

import de.dde.snes.memory.Memory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

class Cartridge(
    val path: Path
) {
    val smcHeaderData: ByteArray
    val data: ByteArray

    val header: CartridgeHeader

    init {
        var b = Files.readAllBytes(path)

        val smcHeader = when (b.size.rem(0x400)) {
            0 -> false
            0x200 -> true
            else -> error("malformed header")
        }

        if (smcHeader) {
            smcHeaderData = b.copyOf(0x200)
            b = b.copyOfRange(0x200, b.size)
        }
        else {
            smcHeaderData = ByteArray(0)
        }

        data = b

        val page = when {
            isLoRomUsed() -> LOROM_HEADER_PAGE
            isHiRomUsed() -> HIROM_HEADER_PAGE
            else -> throw IllegalArgumentException("cartrtidge $path cannot be assigned to any MapMode")
        }

        header = readHeader(page * Memory.PAGE_SIZE + HEADER_START_INDEX)
    }

    private fun isLoRomUsed(): Boolean {
        for (i in 0 until NAME_SIZE) {
            if(!data[(0 * Memory.BANK_SIZE) + (LOROM_HEADER_PAGE * Memory.PAGE_SIZE) + HEADER_START_INDEX + 0x10 + i].isValidChar()) {
                return false
            }
        }

        if (data[(0 * Memory.BANK_SIZE) + (LOROM_HEADER_PAGE * Memory.PAGE_SIZE) + HEADER_START_INDEX + 0x10 + NAME_SIZE].toInt() and 0x37 != 0x20) {
            return false
        }

        return true
    }

    private fun isHiRomUsed(): Boolean {
        for (i in 0 until NAME_SIZE) {
            if(!data[(0 * Memory.BANK_SIZE) + (HIROM_HEADER_PAGE * Memory.PAGE_SIZE) + HEADER_START_INDEX + 0x10 + i].isValidChar()) {
                return false
            }
        }

        if (data[(0 * Memory.BANK_SIZE) + (HIROM_HEADER_PAGE * Memory.PAGE_SIZE) + HEADER_START_INDEX + 0x10 + NAME_SIZE].toInt() and 0x37 != 0x21) {
            return false
        }

        return true
    }

    private fun readHeader(startIndex: Int): CartridgeHeader {
        val buffer = ByteBuffer.wrap(data.copyOfRange(startIndex, startIndex + HEADER_SIZE))
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        val maker = buffer.short
        val game = (0..3).map { buffer.get().toChar() }.joinToString("")
        val fixed = (0..6).map { (buffer.get().toLong() and 0xFF) shl (8 * (6 - it)) }.sum()
        val exRamSize = buffer.get()
        val specVersion = buffer.get()
        val cTypeSub = buffer.get()
        val name = (0 until NAME_SIZE).map { buffer.get().toChar() }.joinToString("").trimEnd()
        val mapMode = buffer.get()
        val cType = buffer.get()
        val romSize = buffer.get()
        val ramSize = buffer.get()
        val destination = buffer.get()
        val fixed2 = buffer.get()
        val maskRomVersion = buffer.get()
        val complementCheck = buffer.short
        val checksum = buffer.short

        return CartridgeHeader(
            maker,
            game,
            fixed,
            exRamSize,
            specVersion,
            cTypeSub,
            name,
            mapMode,
            cType,
            romSize,
            ramSize,
            destination,
            fixed2,
            maskRomVersion,
            complementCheck,
            checksum
        )
    }

    companion object {
        private const val LOROM_HEADER_PAGE = 0x7
        private const val HIROM_HEADER_PAGE = 0xF
        private const val HEADER_SIZE = 0x50
        private const val HEADER_START_INDEX = Memory.PAGE_SIZE - HEADER_SIZE
        private const val NAME_SIZE = 0x15

        private fun Byte.isValidChar() = this in 0x20 until 0x7F
    }
}