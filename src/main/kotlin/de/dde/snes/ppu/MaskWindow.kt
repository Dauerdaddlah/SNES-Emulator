package de.dde.snes.ppu

class MaskWindow {
    var leftPosition = 0
    var rightPosition = 0

    fun reset() {
        leftPosition = 0
        rightPosition = 0
    }

    enum class Logic {
        OR,
        AND,
        XOR,
        XNOR;

        val code get() = ordinal

        companion object {
            fun byCode(code: Int) = values()[code]
        }
    }
}