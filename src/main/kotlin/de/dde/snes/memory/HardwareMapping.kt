package de.dde.snes.memory

import de.dde.snes.*

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
            in 0x4300..0x437F -> readByteDma(bank, address)
            0x4210, 0x4211 -> readByteProcessor(bank, address)
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
            in 0x4300..0x437F -> writeByteDma(bank, address, value)
            0x4210, 0x4211 -> writeByteProcessor(bank, address, value)

            else -> {
                if (!allowInvalidAccess) {
                    error("Invalid Write-Access to Hardware for value ${value.toHexString()} to Address ${bank.toBankString()}:${address.toAddressString()}")
                }
            }
        }
    }

    private fun readByteProcessor(bank: Bank, address: ShortAddress): Int {
        return when (address) {
            0x4210 -> {
                val chipVersion = snes.processor.chipVersion
                val r = chipVersion or (if (snes.processor.nmiFlag) 0x80 else 0)
                snes.processor.nmiFlag = false
                r
            }
            0x4211 -> {
                val r = if (snes.processor.irqFlag) 0x80 else 0
                snes.processor.irqFlag = false
                r
            }
            else -> { println("READ PROCESSOR ${address.toString(16)}"); error("not implemented yet") }
        }
    }

    fun writeByteProcessor(bank: Bank, address: ShortAddress, value: Int) {
        when (address) {
            0x4210, 0x4211 -> {
            }
            else -> { println("WRITE ${value.toString(16)} to PROCESSOR ${address.toString(16)}"); error("not implemented yet")}
        }
    }

    private fun readByteDma(bank: Bank, address: ShortAddress): Int {
        val channel = (address and 0x00F0) shr 8

        with(snes.dma[channel]) {
            return when (address and 0xF) {
                0x0 -> {
                    var r = 0

                    if (transferDirection) r = r or 0x80
                    if (addressModePointer) r = r or 0x40
                    if (addressDecrement) r = r or 0x10
                    if (fixAddress) r = r or 0x08
                    r = r or transferMode.code

                    return r
                }
                0x1 -> {
                    destination
                }
                0x2 -> {
                    sourceAddress.lowByte()
                }
                0x3 -> {
                    sourceAddress.highByte()
                }
                0x4 -> {
                    sourceAddress.longByte()
                }
                0x5 -> {
                    indirectAddress.lowByte()
                }
                0x6 -> {
                    indirectAddress.highByte()
                }
                0x7 -> {
                    indirectAddress.longByte()
                }
                0x8 -> {
                    tableAddress.lowByte()
                }
                0x9 -> {
                    tableAddress.highByte()
                }
                0xA -> {
                    lineCount or (if (repeat) 0x80 else 0x0)
                }
                in 0xB..0xF -> {
                    Memory.OPEN_BUS
                }
                else -> error("invalid dma read for address ${address.toString(16)} on channel $channel")
            }
        }
    }

    private fun writeByteDma(bank: Bank, address: ShortAddress, value: Int) {
        val channel = (address and 0x00F0) shr 8

        with(snes.dma[channel]) {
            when (address and 0xF) {
                0x0 -> {
                    transferDirection = value.isBitSet(0x80)
                    addressModePointer = value.isBitSet(0x40)
                    addressDecrement = value.isBitSet(0x10)
                    fixAddress = value.isBitSet(0x08)
                    transferMode = DMA.TransferMode.byCode(value and 0x7)
                }
                0x1 -> {
                    destination = value.asByte()
                }
                0x2 -> {
                    sourceAddress = sourceAddress.withLow(value.asByte())
                }
                0x3 -> {
                    sourceAddress = sourceAddress.withHigh(value.asByte())
                }
                0x4 -> {
                    sourceAddress = sourceAddress.withLongByte(value.asByte())
                }
                0x5 -> {
                    indirectAddress = indirectAddress.withLow(value.asByte())
                }
                0x6 -> {
                    indirectAddress = indirectAddress.withHigh(value.asByte())
                }
                0x7 -> {
                    indirectAddress = indirectAddress.withLongByte(value.asByte())
                }
                0x8 -> {
                    tableAddress = tableAddress.withLow(value.asByte())
                }
                0x9 -> {
                    tableAddress = tableAddress.withHigh(value.asByte())
                }
                0xA -> {
                    repeat = value.isBitSet(0x80)
                    lineCount = value and 0x7F
                }
                in 0xB..0xF -> {

                }
            }
        }
    }
}