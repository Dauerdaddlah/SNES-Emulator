package de.dde.snes.memory

import de.dde.snes.Cartridge
import de.dde.snes.MapMode

class Memory(
    val cartridge: Cartridge,
    val wram: ByteArray = ByteArray(_1K * 128), // 128 k
    val sram: ByteArray = ByteArray(_1K * cartridge.header.sramSizeKb.toInt()) // max 512 k
) {
    // 64k vram
    // 512b cgram - 256 palette-entries each 1w or 16b
    // 544b oam - 128 sprites in 2 tables - table 1 has 4 byte per sprite (512b) - table 2 has 2 bit per sprite (32b)
    
    /** Memory Data Register - holds the last value read or written. Every time, an invalid address is requested, this value will be used */
    private var mdr: Byte = 0

    private val mapping = with (MemoryMappingOpenPort()) { Array<MemoryMapping>(ADDRESS_SPACE / PAGE_SIZE) { this } }

    init {
        val wramMapping = WRamMapping()
        setMapping(0x7E, 0x7F, 0x0000, 0xFFFF, wramMapping)
        setMapping(0x00, 0x3F, 0x0000, 0x1FFF, wramMapping)
        setMapping(0x80, 0xBF, 0x0000, 0x1FFF, wramMapping)

        val cpuMapping = CpuMapping()
        setMapping(0x00, 0x3F, 0x2100, 0x21FF, cpuMapping)
        setMapping(0x80, 0xBF, 0x2100, 0x21FF, cpuMapping)

        val ppuMapping = PpuMapping()
        setMapping(0x00, 0x3F, 0x4200, 0x43FF, ppuMapping)
        setMapping(0x80, 0xBF, 0x4200, 0x43FF, ppuMapping)

        //setAccess(0x00, 0x3F, 0x4000, 0x4FFF, joypadAccess)
        //setAccess(0x80, 0xBF, 0x4000, 0x4FFF, joypadAccess)

        when (cartridge.header.mapMode) {
            MapMode.LOROM -> {
                val romMapping = LoRomROMMapping()
                setMapping(0x00, 0x7D, 0x8000, 0xFFFF, romMapping)
                setMapping(0x80, 0xFF, 0x8000, 0xFFFF, romMapping)

                val ramMapping = LoRomRAMMapping()
                setMapping(0x70, 0x7D, 0x0000, 0x7FFF, ramMapping)
                setMapping(0xF0, 0xFF, 0x0000, 0x7FFF, ramMapping)
            }
            MapMode.HIROM -> TODO()
            MapMode.SA1ROM,
            MapMode.FAST_LOROM,
            MapMode.FAST_HIROM,
            MapMode.EX_LOROM,
            MapMode.EX_HIROM,
            MapMode.UNKNOWN -> error("Mode ${cartridge.header.mapMode}(${cartridge.header.mapModeRaw}) not supported yet")
        }

        // HiROM
        //setAccess(0x00, 0x3F, 0x8000, 0xFFFF, ROM)
        //setAccess(0x40, 0x7D, 0x0000, 0xFFFF, ROM)
        //setAccess(0x80, 0xBF, 0x8000, 0xFFFF, ROM)
        //setAccess(0xC0, 0xFF, 0x0000, 0xFFFF, ROM)

        //setAccess(0x00, 0x3F, 0x6000, 0x7FFF, RAM)
        //setAccess(0x80, 0xBF, 0x6000, 0x7FFF, RAM)


        //when (cartridge.)
    }

    private fun setMapping(fromBank: Int, toBank: Int, fromAddress: Int, toAddress: Int, mapping: MemoryMapping) {
        for (b in fromBank..toBank) {
            for (a in fromAddress..toAddress step PAGE_SIZE) {
                val i = mappingIndex(b, a)
                this.mapping[mappingIndex(b, a)] = mapping
            }
        }
    }

    private inline fun mappingIndex(bank: Int, address: Int) = bank shl 4 + address

    fun readByte(bank: BankNo, address: ShortAddress): Int {
        val b = mapping[mappingIndex(bank.bankNo, address.shortAddress)].readByte(this, bank, address)

        if (b != -1) {
            mdr = b.toByte()
        }

        return mdr.toInt() and 0xFF
    }

    fun writeByte(bank: BankNo, address: ShortAddress, value: Int) {
        mdr = value.toByte()

        mapping[mappingIndex(bank.bankNo, address.shortAddress)].writeByte(this, bank, address, value)
    }


    inner class MemoryMappingOpenPort : MemoryMapping {
        override fun readByte(memory: Memory, bank: BankNo, address: ShortAddress): Int {
            return mdr.toInt()
        }

        override fun writeByte(memory: Memory, bank: BankNo, address: ShortAddress, value: Int) {
            mdr = value.toByte()
        }

    }

    companion object {
        private const val _1K = 0x400
        private const val _1M = _1K * _1K

        const val ADDRESS_SPACE = 0x1000000
        const val PAGE_SIZE = 0x1000
        const val BANK_SIZE = 0x10000
    }
}

inline fun bankNo(bankNo: Int): BankNo =
    BankNo(bankNo and 0xFF)
inline fun shortAddress(shortAddress: Int): ShortAddress =
    ShortAddress(shortAddress and 0xFFFF)
inline fun fullAddress(fullAddress: Int): FullAddress =
    FullAddress(fullAddress and 0xFFFFFF)

inline class BankNo(val bankNo: Int)

inline class ShortAddress(val shortAddress: Int)

inline class FullAddress(val fullAddress: Int) {

    constructor(bankNo: BankNo, shortAddress: ShortAddress) : this((bankNo.bankNo shl 16) + (shortAddress.shortAddress))

    val bankNo: BankNo
        get() = bankNo(
            fullAddress ushr 16
        )
    val shortAaddress: ShortAddress
        get() = shortAddress(
            fullAddress
        )
}