package de.dde.snes

sealed class MemoryMapping {
    abstract fun isUsed(cartridge: Cartridge): Boolean

    abstract fun byte(memory: Memory, bank: Int, address: Int): Byte

    abstract fun readHeader(cartridge: Cartridge): CartridgeHeader
}

object LoRom : MemoryMapping() {
    override fun isUsed(cartridge: Cartridge): Boolean {
        for (i in 0 until NAME_SIZE) {
            if(!cartridge.data[(0 * BANK_SIZE) + (LOROM_HEADER_PAGE * PAGE_SIZE) + HEADER_START_INDEX + 0x10 + i].isValidChar()) {
                return false
            }
        }

        if (cartridge.data[(0 * BANK_SIZE) + (LOROM_HEADER_PAGE * PAGE_SIZE) + HEADER_START_INDEX + 0x10 + NAME_SIZE].toInt() and 0x37 != 0x20) {
            return false
        }

        return true
    }

    override fun byte(memory: Memory, bank: Int, address: Int): Byte {
        return when {
            // special address in lower half, the rest is copy in both halfes
            bank in 0x7E..0x7F -> {
                // wram
                val b = bank - 0x7E

                val a = address + b * BANK_SIZE
                return byte(memory, memory.wram, a)
            }
            // in every other case copy from left half
            bank < 0x7e -> {
                //copy of left half
                byte(memory, bank + 0x80, address)
            }

            // lorom specifics - upper half is completely rom
            address >= 0x8000 -> {
                // rom access
                val b = bank - 0x80

                val a = address - 0x8000 + b * BANK_SIZE / 2
                return memory.cartridge.data[a]
            }
            // lower half of the rest
            // right side of the lower half
            bank < 0xC0 && address < 0x2000 -> {
                // copy of wram
                byte(memory, 0x7e, address)
            }
            bank < 0xC0 && address < 0x6000 -> {
                // hardware register
                // TODO
                //return memory.readHardwareRegister(address)
                return 0
            }
            bank < 0xC0 -> {
                // open bus
                return -1
            }
            bank < 0xF0 -> {
                // copy of rom
                return byte(memory, bank, address + 0x8000)
            }
            bank in 0xF0..0xFF -> {
                // sram copies itself along
                // sram
                val b = bank - 0xF0

                val a = address + b * BANK_SIZE / 2
                return memory.sram[a]
            }

            else -> -1
        }
    }

    private fun byte(memory: Memory, data: ByteArray, index: Int): Byte {
        return if (index < data.size) data[index] else -1
    }

    override fun readHeader(cartridge: Cartridge): CartridgeHeader {
        cartridge.data.position(LOROM_HEADER_PAGE * PAGE_SIZE + HEADER_START_INDEX)

        val buffer = cartridge.data

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

        return CartridgeHeader(maker, game, fixed, exRamSize, specVersion, cTypeSub, name, mapMode, cType, romSize, ramSize, destination, fixed2, maskRomVersion, complementCheck, checksum)
    }

    private const val LOROM_HEADER_PAGE = 0x7
}

object HiRom : MemoryMapping() {
    override fun isUsed(cartridge: Cartridge): Boolean {
        for (i in 0 until NAME_SIZE) {
            if(!cartridge.data[(0 * BANK_SIZE) + (HIROM_HEADER_PAGE * PAGE_SIZE) + HEADER_START_INDEX + 0x10 + i].isValidChar()) {
                return false
            }
        }

        if (cartridge.data[(0 * BANK_SIZE) + (HIROM_HEADER_PAGE * PAGE_SIZE) + HEADER_START_INDEX + 0x10 + NAME_SIZE].toInt() and 0x37 != 0x21) {
            return false
        }

        return true
    }

    override fun byte(memory: Memory, bank: Int, address: Int): Byte {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readHeader(cartridge: Cartridge): CartridgeHeader {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private const val HIROM_HEADER_PAGE = 0xF
}

val mappings = listOf(LoRom, HiRom)

private const val BANK_SIZE = 0x10000
private const val PAGE_SIZE = 0x1000

private const val NAME_SIZE = 0x15
private const val HEADER_START_INDEX = PAGE_SIZE - 0x50

private fun Byte.isValidChar() = this in 0x20 until 0x7F