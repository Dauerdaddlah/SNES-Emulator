package de.dde.snes.cpu

import de.dde.snes.*
import de.dde.snes.memory.Bank
import de.dde.snes.memory.Memory
import de.dde.snes.memory.MemoryMapping
import de.dde.snes.memory.ShortAddress

class CPU : MemoryMapping {
    val wram = WRAM()

    var nmiEnabled = false
    var yIrqEnabled = false
    var xIrqEnabled = false
    var autoJoypadRead = false

    /** if xirq is enabled and hcounter reaches this value an irq is fired (yirq is considered as well) */
    var htime = 0x1FF
    /** if yirq is enabled and vcounter reaches this value an irq is fired (xirq is considered as well) */
    var vtime = 0x1FF

    var multiplicantA = 0xFF
    var multiplicantB = 0
    var product = 0

    var divident = 0xFFFF
    var divisor = 0
    var quotient = 0
    var remainder = 0

    var multiplicationDone = false

    var fastRom = false

    fun reset() {
        wram.reset()

        nmiEnabled = false
        yIrqEnabled = false
        xIrqEnabled = false
        autoJoypadRead = false

        htime = 0x1FF
        vtime = 0x1FF

        multiplicantA = 0xFF
        multiplicantB = 0
        product = 0

        divident = 0xFFFF
        divisor = 0
        quotient = 0
        remainder = 0

        multiplicationDone = false

        fastRom = false
    }

    override fun readByte(snes: SNES, bank: Bank, address: ShortAddress): Int {
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
            0x4210 -> {
                // TODO
                -1
            }
            0x4211 -> {
                // TODO
                -1
            }
            0x4212 -> {
                // TODO
                -1
            }
            0x4214 -> {
                quotient.lowByte()
            }
            0x4215 -> {
                quotient.highByte()
            }
            0x4216 -> {
                if (multiplicationDone) {
                    product.lowByte()
                } else {
                    remainder.lowByte()
                }
            }
            0x4217 -> {
                if (multiplicationDone) {
                    product.highByte()
                } else {
                    remainder.highByte()
                }
            }
            else -> { println("READ CPU ${address.toString(16)}"); error("not implemented yet")}
        }
    }

    override fun writeByte(snes: SNES, bank: Bank, address: ShortAddress, value: Int) {
        when (address) {
            0x2180 -> {
                wram.write(value)
            }
            in 0x2181..0x2183 -> {
            }
            0x4200 -> {
                nmiEnabled = value.isBitSet( 0x80)
                yIrqEnabled = value.isBitSet(0x20)
                xIrqEnabled = value.isBitSet(0x10)
                autoJoypadRead = value.isBitSet(0x01)
            }
            0x4202 -> {
                multiplicantA = value.asByte()
            }
            0x4203 -> {
                multiplicantB = value.asByte()
                product = multiplicantA * multiplicantB
                multiplicationDone = true
            }
            0x4204 -> {
                divident = divident.withLow(value.asByte())
            }
            0x4205 -> {
                divident = divident.withHigh(value.asByte())
            }
            0x4206 -> {
                divisor = value.asByte()

                quotient = divident / divisor
                remainder = divident - (quotient * divisor)
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
                    if (value.isBitSet(i)) {
                        dma.startHdma()
                    }
                    i = i shl 1
                }
            }
            0x420D -> {
                fastRom = value.isBitSet(0x1)
            }
            0x4210 -> {
            }
            else -> { println("WRITE ${value.toString(16)} to CPU ${address.toString(16)}"); error("not implemented yet")}
        }
    }
}