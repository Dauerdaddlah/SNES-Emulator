package de.dde.snes.ppu

import de.dde.snes.Short
import de.dde.snes.isBitSet

class OAM {
    val oam = ByteArray(544)

    var objPrio = false
    var tableSelect = 0
        set(value) {
            field = value
            oamReset()
        }
    var address = 0
        set(value) {
            field = value
            oamReset()
        }

    private var internalAddress = 0
    private var high = false
    private var tempVal = 0

    fun reset() {
        objPrio = false
        tableSelect = 0
        address = 0

        oamReset()
    }

    fun oamReset() {
        internalAddress = if (tableSelect == 1) {
            Short(address.rem(0x10), tableSelect)
        } else {
            address
        }
        internalAddress = internalAddress shl 1
        //internalAddress = Short(address, tableSelect) shl 1 // shl 1 or *2 because it is a word-address and we store bytes
        high = false
        tempVal = 0
    }

    fun write(value: Int) {
        if (internalAddress.isBitSet(0x200)) {
            // we are pointing ot the high-table
            oam[internalAddress] = value.toByte()
        } else {
            // we are pointing at the low table
            if (high) {
                oam[internalAddress - 1] = tempVal.toByte()
                oam[internalAddress] = value.toByte()
            } else {
                tempVal = value
            }
        }

        internalAddress++
        // TODO is this handling correct?
        if (internalAddress == 544) {
            internalAddress = 0
        }
        high = !high
    }

    fun read(): Int {
        val r = oam[internalAddress].toInt()
        internalAddress++
        high = !high
        return r
    }
}