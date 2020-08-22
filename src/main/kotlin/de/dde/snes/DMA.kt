package de.dde.snes

import de.dde.snes.memory.*

class DMA(
    val snes: SNES,
    val channel: Int
) {
    /** if true data will be transferred from ppu to cpu */
    var transferDirection = true
    /** if true the hdma-table contains pointer, otherwise it contains the raw data */
    var addressModeIndirect = true
    /** if true the address will be decremented, otherwise, it will be incremented */
    var addressDecrement = true
    /** if true, the dma-address will not be adjusted */
    var fixAddress = true
    var transferMode = TransferMode.byCode(7)

    /** 8-bit hardware address */
    var destination = 0
    /** 24-bit cpu-address - in hdma address of the hdma-table*/
    var sourceAddress = 0
    /** in dma the first 16 bit set the number of bytes to transfer - in hdma  */
    var indirectAddress = 0
    /** the bank used for indirect addressing in hdma */
    var indirectBank = 0
    /**  */
    var tableAddress = 0
    var repeat = false
    var lineCount = 0

    var size: Int
        get() = indirectAddress.asShort()
        set(value) {
            indirectAddress = value
        }

    var hdmaRequested = false

    var inDma = false
        private set
    var inHdma = false
        private set

    fun reset() {
        transferDirection = true
        addressModeIndirect = true
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

    fun doHdmaForScanline(scanline: Int) {
        if (!hdmaRequested) {
            return
        }

        var firstLineOfEntry = false
        if (scanline == 0) {
            // on the first line of a frame initialize everything
            if (readNextEntryHead()) {
                tableAddress = sourceAddress.shortAddress
                inHdma = true
                firstLineOfEntry = true
            } else {
                inHdma = false
                return
            }
        } else if (!inHdma) {
            // we already reached the end of the hdma-table previously
            return
        }

        lineCount--

        if (lineCount == 0) {
            if (!readNextEntryHead()) {
                inHdma = false
                return
            }
            firstLineOfEntry = true
        }

        if (firstLineOfEntry || repeat) {
            val adjusts = transferMode.buildAddressAdjusts()

            if (addressModeIndirect) {
                // copy indirect address to own field (registers)
                indirectAddress = snes.memory.readByte(sourceAddress.bank, tableAddress)
                tableAddress = shortAddress(tableAddress + 1)
                indirectAddress = indirectAddress.withHigh(snes.memory.readByte(sourceAddress.bank, tableAddress))
                tableAddress = shortAddress(tableAddress + 1)
            }

            repeat (transferMode.totalWrites) {
                val adjust = adjusts[it.rem(adjusts.size)]

                val dest = ((destination + adjust) and 0xFF) + 0x2100

                val v = if (addressModeIndirect) {
                    val vv = snes.memory.readByte(indirectBank, indirectAddress)
                    indirectAddress = shortAddress(indirectAddress + 1)
                    vv
                } else {
                    val vv = snes.memory.readByte(sourceAddress.bank, tableAddress)
                    tableAddress = shortAddress(tableAddress + 1)
                    vv
                }

                if (transferDirection) {
                    error("hdma transfer from ppu to cpu not supported yet")
                } else {
                    snes.memory.writeByte(0, dest,
                        v)
                }
            }
        }
    }

    private fun readNextEntryHead(): Boolean {
        val b = snes.memory.readByte(sourceAddress.bank, tableAddress)

        if (b == 0) {
            return false
        }

        // before the header is checked at all, the byte is decremented
        //
        // this means a value of 0x80 does not make repeat with count 0, but no repeat with count 0x7F
        // therefore the effective value of repeat needs to be calculated from b - 1
        // and the effective value from lineCount is (b & 0x7F) - 1
        //
        // in the special case of 0x80 the lineCount would be 0x7F (0x80 - 1)
        // we calculate those both with (b - 1) & 0x7F

        repeat = (b - 1).isBitSet(0x80)
        lineCount = ((b - 1) and 0x7F)

        // add one, as this is the effective count, which shall be seen after the first decrement
        lineCount++

        tableAddress = shortAddress(tableAddress + 1)

        return true
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