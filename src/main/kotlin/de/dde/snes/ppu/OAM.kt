package de.dde.snes.ppu

class OAM {
    val oam = ByteArray(544)

    var objPrio = false
    var tableSelect = 0 // which table should be selected in OAM
    var address = 0

    fun reset() {
        objPrio = false
        tableSelect = 0
        address = 0
    }
}