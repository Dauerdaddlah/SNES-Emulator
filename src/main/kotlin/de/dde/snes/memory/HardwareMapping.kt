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
            in 0x4214..0x4217 -> readByteCpu(bank, address)
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
            in 0x4214..0x4217 -> writeByteCpu(bank, address, value)
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

    private fun readByteCpu(bank: Bank, address: ShortAddress): Int {
        with (snes.processor) {
            return when (address) {
                0x2180 -> {
                    wram.read()
                }
                in 0x2181..0x2183 -> {
                    Memory.OPEN_BUS
                }
                0x4200 -> {
                    var r = 0
                    if (nmiEnabled) r = r.setBit(0x80)
                    if (yIrqEnabled) r = r.setBit(0x20)
                    if (xIrqEnabled) r = r.setBit(0x10)
                    if (autoJoypadRead) r = r.setBit(0x01)
                    return r
                }
                in 0x4202..0x420A -> {
                    Memory.OPEN_BUS
                }
                0x420B -> {
                    var r = 0
                    var i = 0x1
                    for (dma in snes.dma) {
                        if (dma.inDma) {
                            r = r.setBit(i)
                        }
                        i = i shl 1
                    }
                    return r
                }
                0x420C -> {
                    var r = 0
                    var i = 0x1
                    for (dma in snes.dma) {
                        if (dma.inHdma) {
                            r = r.setBit(i)
                        }
                        i = i shl 1
                    }
                    return r
                }
                0x420D -> {
                    Memory.OPEN_BUS
                }
                0x4212 -> {
                    var r = 0
                    if (snes.ppu.inVBlank) r = r or 0x80
                    if (snes.ppu.inHBlank) r = r or 0x40
                    if (snes.controllers.autoJoypadReadActive) r = r or 0x01
                    r
                }
                0x4214 -> {
                    division.quotient.lowByte()
                }
                0x4215 -> {
                    division.quotient.highByte()
                }
                0x4216 -> {
                    if (multiplicationDone) {
                        multiplication.product.lowByte()
                    } else {
                        division.remainder.lowByte()
                    }
                }
                0x4217 -> {
                    if (multiplicationDone) {
                        multiplication.product.highByte()
                    } else {
                        division.remainder.highByte()
                    }
                }
                else -> {
                    println("READ CPU ${address.toString(16)}"); error("not implemented yet")
                }
            }
        }
    }

    private fun writeByteCpu(bank: Bank, address: ShortAddress, value: Int) {
        with (snes.processor) {
            when (address) {
                0x2180 -> {
                    wram.write(value)
                }
                in 0x2181..0x2183 -> {
                }
                0x4200 -> {
                    nmiEnabled = value.isBitSet(0x80)
                    yIrqEnabled = value.isBitSet(0x20)
                    xIrqEnabled = value.isBitSet(0x10)
                    autoJoypadRead = value.isBitSet(0x01)
                }
                0x4202 -> {
                    multiplication.multiplicantA = value.asByte()
                }
                0x4203 -> {
                    multiplication.multiplicantB = value.asByte()
                    multiplication.product = multiplication.multiplicantA * multiplication.multiplicantB
                    multiplicationDone = true
                }
                0x4204 -> {
                    division.divident = division.divident.withLow(value.asByte())
                }
                0x4205 -> {
                    division.divident = division.divident.withHigh(value.asByte())
                }
                0x4206 -> {
                    division.divisor = value.asByte()

                    if (division.divisor == 0) {
                        division.quotient = 0
                        division.remainder = 0
                    } else {
                        division.quotient = division.divident / division.divisor
                        division.remainder = division.divident - (division.quotient * division.divisor)
                    }
                    multiplicationDone = false
                }
                0x4207 -> {
                    htime = htime.withLow(value.asByte())
                }
                0x4208 -> {
                    htime = htime.withHigh(value.asByte())
                }
                0x4209 -> {
                    vtime = vtime.withLow(value.asByte())
                }
                0x420A -> {
                    vtime = vtime.withHigh(value.asByte())
                }
                0x420B -> {
                    var i = 0x1
                    for (dma in snes.dma) {
                        if (value.isBitSet(i)) {
                            dma.startDma()
                        }
                        i = i shl 1
                    }
                }
                0x420C -> {
                    var i = 0x1
                    for (dma in snes.dma) {
                        dma.hdmaRequested = value.isBitSet(i)
                        i = i shl 1
                    }
                }
                0x420D -> {
                    fastRom = value.isBitSet(0x1)
                }
                0x4212,
                in 0x4214..0x4217 -> {
                }
                else -> {
                    println("WRITE ${value.toString(16)} to CPU ${address.toString(16)}"); error("not implemented yet")
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
                    if (addressModeIndirect) r = r or 0x40
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
                    indirectBank
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
                    addressModeIndirect = value.isBitSet(0x40)
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
                    indirectBank = value.asByte()
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