package de.dde.snes.memory

import de.dde.snes.SNES
import de.dde.snes.asByte
import de.dde.snes.cartridge.Cartridge
import de.dde.snes.cartridge.MapMode

class MemoryImpl(
    private val snes: SNES
) : Memory {
    // 64k vram
    // 512b cgram - 256 palette-entries each 1w or 16b
    // 544b oam - 128 sprites in 2 tables - table 1 has 4 byte per sprite (512b) - table 2 has 2 bit per sprite (32b)
    var sram: ByteArray = ByteArray(0)
        private set

    /** Memory Data Register - holds the last value read or written. Every time, an invalid address is requested, this value will be used */
    private var mdr: Int = 0

    private val mapping = with (MemoryMappingOpenPort()) { Array<MemoryMapping>(
        Memory.ADDRESS_SPACE / Memory.PAGE_SIZE
    ) { this } }

    override fun reset() {
        sram = ByteArray(0)
    }

    override fun initializeFor(cartridge: Cartridge) {
        sram = ByteArray(Memory._1K * cartridge.header.sramSizeKb.toInt()) // max 512 k

        val wramMapping = WRamMapping(snes.processor.wram.wram)
        setMapping(0x7E, 0x7F, 0x0000, 0xFFFF, wramMapping)
        setMapping(0x00, 0x3F, 0x0000, 0x1FFF, wramMapping)
        setMapping(0x80, 0xBF, 0x0000, 0x1FFF, wramMapping)

        val hardwareMapping = HardwareMapping(snes, false)
        setMapping(0x00, 0x3F, 0x2100, 0x21FF, hardwareMapping)
        setMapping(0x80, 0xBF, 0x2100, 0x21FF, hardwareMapping)
        setMapping(0x00, 0x3F, 0x4200, 0x43FF, hardwareMapping)
        setMapping(0x80, 0xBF, 0x4200, 0x43FF, hardwareMapping)

        //setAccess(0x00, 0x3F, 0x4000, 0x4FFF, joypadAccess)
        //setAccess(0x80, 0xBF, 0x4000, 0x4FFF, joypadAccess)

        when (cartridge.header.mapMode) {
            MapMode.LOROM -> {
                val romMapping = LoRomROMMapping(cartridge.data)
                val ramMapping = LoRomRAMMapping(sram)

                setMapping(0x00, 0x7D, 0x8000, 0xFFFF, romMapping) // upper right quarter complete without wram
                setMapping(0x40, 0x6F, 0x0000, 0x7FFF, romMapping) // right side, lower half, left quarter without sram
                setMapping(0x70, 0x7D, 0x0000, 0x7FFF, ramMapping) // right side, lower half, left quarter without wram

                setMapping(0x80, 0xFF, 0x8000, 0xFFFF, romMapping) // upper left quarter complete
                setMapping(0xC0, 0xEF, 0x0000, 0x7FFF, romMapping) // left side, lower half, left quarter without sram
                setMapping(0xF0, 0xFF, 0x0000, 0x7FFF, ramMapping) // left side, lower hald, left side
            }
            MapMode.HIROM -> {
                val romMapping = HiRomROMMapping(cartridge.data)
                val ramMapping = HiRomRAMMapping(sram)

                setMapping(0x00, 0x3F, 0x8000, 0xFFFF, romMapping) // right side, right upper quarter
                setMapping(0x00, 0x3F, 0x6000, 0x7FFF, ramMapping) // right side, ram in lower right quarter
                setMapping(0x40, 0x7D, 0x0000, 0xFFFF, romMapping) // right side, left half complete

                setMapping(0x80, 0xBF, 0x8000, 0xFFFF, romMapping) // left side, right upper quarter
                setMapping(0x80, 0xBF, 0x6000, 0x7FFF, ramMapping) // left side ram in lower right quarter
                setMapping(0xC0, 0xFF, 0x0000, 0xFFFF, romMapping) // left side, left half complete
            }
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
            for (a in fromAddress..toAddress step Memory.PAGE_SIZE) {
                this.mapping[mappingIndex(b, a)] = mapping
            }
        }
    }

    private fun mappingIndex(bank: Int, address: Int) = (bank shl 4) or (address shr 12)

    override fun readByte(bank: Bank, address: ShortAddress): Int {
        assert(bank in 0x0..0xFF)
        assert(address in 0x0..0xFFFF)

        val b = mapping[mappingIndex(bank, address.shortAddress)].readByte(bank, address)

        if (b != Memory.OPEN_BUS) {
            mdr = b.asByte()
        }

        return mdr
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        assert(bank in 0x0..0xFF)
        assert(address in 0x0..0xFFFF)

        mdr = value.asByte()

        mapping[mappingIndex(bank, address.shortAddress)].writeByte(bank, address, value)
    }


    inner class MemoryMappingOpenPort : MemoryMapping {
        override fun readByte(bank: Bank, address: ShortAddress): Int {
            return mdr
        }

        override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        }

    }
}