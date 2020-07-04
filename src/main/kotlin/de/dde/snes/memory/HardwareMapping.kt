package de.dde.snes.memory

import de.dde.snes.SNES

class HardwareMapping : MemoryMapping {
    override fun readByte(snes: SNES, bank: Bank, address: ShortAddress): Int {
        // TODO
        return when (address) {
            in 0x2140..0x2143 -> snes.apu.readByte(snes, bank, address)
            in 0x2180..0x2183,
            0x4200,
            in 0x4202..0x420D,
            in 0x4210..0x4212,
            in 0x4214..0x4217 -> snes.cpu.readByte(snes, bank, address)
            in 0x2100..0x2132,
            in 0x2134..0x213F -> snes.ppu.readByte(snes, bank, address)

            in 0x2100..0x2143,
            in 0x4200..0x420D,
            in 0x4210..0x421F,
            in 0x4300..0x430a,
            in 0x4310..0x431a,
            in 0x4320..0x432a,
            in 0x4330..0x433a,
            in 0x4340..0x434a,
            in 0x4350..0x435a,
            in 0x4360..0x436a,
            in 0x4370..0x437a-> { println("read address ${address.toString(16)}"); error("not implemented yet") }
            else -> -1
        }
    }

    override fun writeByte(snes: SNES, bank: Bank, address: ShortAddress, value: Int) {
        // TODO
        when (address) {
            in 0x2140..0x2143 -> snes.apu.writeByte(snes, bank, address, value)
            in 0x2180..0x2183,
            0x4200,
            in 0x4202..0x420D,
            in 0x4210..0x4212,
            in 0x4214..0x4217 -> snes.cpu.writeByte(snes, bank, address, value)
            in 0x2100..0x2132,
            in 0x2134..0x213F -> snes.ppu.writeByte(snes, bank, address, value)

            in 0x2100..0x2143,
            in 0x4200..0x420D,
            in 0x4210..0x421F,
            in 0x4300..0x430a,
            in 0x4310..0x431a,
            in 0x4320..0x432a,
            in 0x4330..0x433a,
            in 0x4340..0x434a,
            in 0x4350..0x435a,
            in 0x4360..0x436a,
            in 0x4370..0x437a-> { println("WRITE ${value.toString(16)} to HARDWARE ${address.toString(16)}"); error("not implemented yet")}
        }
    }
}