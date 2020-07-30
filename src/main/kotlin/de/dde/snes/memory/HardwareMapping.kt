package de.dde.snes.memory

import de.dde.snes.SNES
import de.dde.snes.toAddressString
import de.dde.snes.toBankString
import de.dde.snes.toHexString

class HardwareMapping(
    private val snes: SNES,
    private val allowInvalidAccess: Boolean = true
) : MemoryMapping {
    override fun readByte(bank: Bank, address: ShortAddress): Int {
        return when (address) {
            in 0x2100..0x213F -> snes.ppu.readByte(bank, address)
            in 0x2140..0x2143 -> snes.apu.readByte(bank, address)
            in 0x2180..0x2183,
            0x4200,
            in 0x4202..0x420D,
            0x4212,
            in 0x4214..0x4217 -> snes.cpu.readByte(bank, address)
            in 0x4016..0x4017,
            0x4201, 0x4213,
            in 0x4218..0x421F-> snes.controllers.readByte(bank, address)
            in 0x4300..0x430F -> snes.dma[0].readByte(bank, address)
            in 0x4310..0x431F -> snes.dma[1].readByte(bank, address)
            in 0x4320..0x432F -> snes.dma[2].readByte(bank, address)
            in 0x4330..0x433F -> snes.dma[3].readByte(bank, address)
            in 0x4340..0x434F -> snes.dma[4].readByte(bank, address)
            in 0x4350..0x435F -> snes.dma[5].readByte(bank, address)
            in 0x4360..0x436F -> snes.dma[6].readByte(bank, address)
            in 0x4370..0x437F -> snes.dma[7].readByte(bank, address)
            0x4210, 0x4211 -> snes.processor.readByte(bank, address)
            else -> {
                if (allowInvalidAccess) {
                    Memory.OPEN_BUS
                } else {
                    error("invalid Read-Access to hardware address ${bank.toBankString()}:${address.toAddressString()}")
                }
            }
        }
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        when (address) {
            in 0x2100..0x213F -> snes.ppu.writeByte(bank, address, value)
            in 0x2140..0x2143 -> snes.apu.writeByte(bank, address, value)
            in 0x2180..0x2183,
            0x4200,
            in 0x4202..0x420D,
            0x4212,
            in 0x4214..0x4217 -> snes.cpu.writeByte(bank, address, value)
            in 0x4016..0x4017,
            0x4201, 0x4213,
            in 0x4218..0x421F -> snes.controllers.writeByte(bank, address, value)
            in 0x4300..0x430F -> snes.dma[0].writeByte(bank, address, value)
            in 0x4310..0x431F -> snes.dma[1].writeByte(bank, address, value)
            in 0x4320..0x432F -> snes.dma[2].writeByte(bank, address, value)
            in 0x4330..0x433F -> snes.dma[3].writeByte(bank, address, value)
            in 0x4340..0x434F -> snes.dma[4].writeByte(bank, address, value)
            in 0x4350..0x435F -> snes.dma[5].writeByte(bank, address, value)
            in 0x4360..0x436F -> snes.dma[6].writeByte(bank, address, value)
            in 0x4370..0x437F -> snes.dma[7].writeByte(bank, address, value)
            0x4210, 0x4211 -> snes.processor.writeByte(bank, address, value)

            else -> {
                if (!allowInvalidAccess) {
                    error("Invalid Write-Access to Hardware for value ${value.toHexString()} to Address ${bank.toBankString()}:${address.toAddressString()}")
                }
            }
        }
    }
}