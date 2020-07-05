package de.dde.snes.controller

class Joypad : ControllerNoIo() {
    var a = false
    var b = false
    var x = false
    var y = false
    var start = false
    var select = false
    var l = false
    var r = false
    var up = false
    var down = false
    var left = false
    var right = false

    private var index = 16
    private val state = BooleanArray(16)

    override fun reset() {
    }

    override fun strobe(high: Boolean) {
        if (high) {
            state[0] = b
            state[1] = y
            state[2] = select
            state[3] = start
            state[4] = up
            state[5] = down
            state[6] = left
            state[7] = right
            state[8] = a
            state[9] = x
            state[10] = l
            state[11] = r
            state[12] = false
            state[13] = false
            state[14] = false
            state[15] = false
            index = 0
        }
    }

    override fun clockPulse() {
        index++
    }

    override fun readData1(): Boolean {
        return if (index < 16) state[index] else false
    }

    override fun readData2(): Boolean {
        return false
    }
}