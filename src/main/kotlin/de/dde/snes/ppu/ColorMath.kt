package de.dde.snes.ppu

class ColorMath {
    var clipMode = ExecutionMode.byCode(0)
    var preventMode = ExecutionMode.byCode(0)
    var addSubscreen = false
    var directColorMode = false
    var addSubtract = AddSubtract.byCode(0)
    var halfColorMath = false

    fun reset() {
        clipMode = ExecutionMode.byCode(0)
        preventMode = ExecutionMode.byCode(0)
        addSubscreen = false
        directColorMode = false
        addSubtract = AddSubtract.byCode(0)
        halfColorMath = false
    }

    enum class ExecutionMode {
        NEVER,
        OUTSIDE,
        INSIDE,
        ALWAYS;

        val code get() = ordinal

        companion object {
            fun byCode(code: Int) = values()[code]
        }
    }

    enum class AddSubtract {
        ADD,
        SUBTRACT;

        val code get() = ordinal

        companion object {
            fun byCode(code: Int) = ExecutionMode.values()[code]
        }
    }
}