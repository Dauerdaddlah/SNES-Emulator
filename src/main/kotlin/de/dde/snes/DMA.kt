package de.dde.snes

import de.dde.snes.memory.*

class DMA(
    val snes: SNES,
    val channel: Int
) : MemoryMapping {
    /** if true data will be transferred from ppu to cpu */
    var transferDirection = true
    /** if true the hdma-table contains pointer, otherwise it contains the raw data */
    var addressModePointer = true
    /** if true the address will be decremented, otherwise, it will be incremented */
    var addressDecrement = true
    /** if true, the dma-address will not be adjusted */
    var fixAddress = true
    var transferMode = TransferMode.byCode(7)

    /** 8-bit hardware address */
    var destination = 0
    /** 24-bit cpu-address */
    var sourceAddress = 0
    var indirectAddress = 0
    var tableAddress = 0
    var repeat = false
    var lineCount = 0

    var size: Int
        get() = indirectAddress.asShort()
        set(value) {
            indirectAddress = (indirectAddress and 0xFF0000) or value
        }

    var hdmaRequested = false

    var inDma = false
        private set
    var inHdma = false
        private set

    fun reset() {
        transferDirection = true
        addressModePointer = true
        addressDecrement = true
        transferMode = TransferMode.byCode(7)
        destination = 0
        sourceAddress = 0
        indirectAddress = 0
        tableAddress = 0
        repeat = false
        lineCount = 0

        hdmaRequested = false
        inDma = false
        inHdma = false
    }

    fun startDma() {
        inDma = true
        val addressUpdate = if (fixAddress) 0 else if (addressDecrement) -1 else 1

        val addressAdjust = transferMode.buildAddressAdjusts()
        var addressAdjustIndex = -1

        do {
            addressAdjustIndex++
            if (addressAdjustIndex == addressAdjust.size) {
                addressAdjustIndex = 0
            }

            val dest = ((destination + addressAdjust[addressAdjustIndex]) and 0xFF) + 0x2100

            if (transferDirection) {
                snes.memory.writeByte(sourceAddress.bank, sourceAddress.shortAddress,
                    snes.memory.readByte(0, dest))
            } else {
                snes.memory.writeByte(0, dest,
                    snes.memory.readByte(sourceAddress.bank, sourceAddress.shortAddress))
            }

            // the bank is not effected
            sourceAddress = (sourceAddress and 0xFF0000) or ((sourceAddress + addressUpdate) and 0xFFFF)

            if (size == 0) {
                size = 0xFFFF
            } else {
                size--
            }
        } while (size > 0)
    }

    fun startHdma() {
        inDma = true
        inHdma = true

        // TODO HDMA-logic
        error("not implemented yet")
    }

    override fun readByte(bank: Bank, address: ShortAddress): Int {
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
                0xFF
            }
            else -> error("invalid dma read for address ${address.toString(16)} on channel $channel")
        }
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        when (address and 0xF) {
            0x0 -> {
                transferDirection = value.isBitSet(0x80)
                addressModePointer = value.isBitSet(0x40)
                addressDecrement = value.isBitSet(0x10)
                fixAddress = value.isBitSet(0x08)
                transferMode = TransferMode.byCode(value and 0x7)
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

    enum class TransferMode {
        R1W1,
        R2W1,
        R1W2,
        R2W2_EACH,
        R4W1,
        R2W2_ALTERNATE,
        R1W2_2,
        R2W2_EACH_2;

        val code get() = ordinal

        val numRegisters get() = when (this) {
            R1W1,
            R1W2,
            R1W2_2 -> 1
            R2W1,
            R2W2_EACH,
            R2W2_EACH_2,
            R2W2_ALTERNATE -> 2
            R4W1 -> 4
        }

        val numWrites get() = when (this) {
            R1W1,
            R2W1,
            R4W1 ->  1
            R1W2,
            R2W2_EACH,
            R2W2_ALTERNATE,
            R1W2_2,
            R2W2_EACH_2 -> 2
        }

        val totalWrites get() = numWrites * numRegisters

        val alternate get() = this == R2W2_ALTERNATE
        val each get() = this == R2W2_EACH || this == R2W2_EACH_2

        fun buildAddressAdjusts() = when(this) {
            R1W1,
            R1W2_2,
            R1W2 -> intArrayOf(0)
            R2W2_ALTERNATE,
            R2W1 -> intArrayOf(0, 1)
            R4W1 ->  intArrayOf(0, 1, 2, 3)
            R2W2_EACH,
            R2W2_EACH_2 -> intArrayOf(0, 0, 1, 1)
        }

        companion object {
            fun byCode(code: Int) = values()[code]
        }

    }
}