package de.dde.snes

class DMA(
    val channel: Int
) {
    var inDma = false
        private set
    var inHdma = false
        private set

    fun startDma() {
        inDma = true
    }

    fun startHdma() {
        inDma = true
        inHdma = true
    }
}