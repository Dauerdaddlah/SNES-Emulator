package de.dde.snes.ppu

import de.dde.snes.asByte
import de.dde.snes.highByte
import de.dde.snes.lowByte
import de.dde.snes.memory.Memory
import de.dde.snes.memory.ShortAddress

class VRAM {
    val vram = ByteArray(64 * Memory._1K)

    var address = 0
    /** if true icrement after low read/write, else after high */
    var addressIncrementMode = IncrementMode.LOW
    var increment = Increment.byCode(0)
    var mapping = Mapping.byCode(0)
    var dataWrite = 0

    fun reset() {
        vram.fill(0)
        addressIncrementMode = IncrementMode.LOW
        increment = Increment.byCode(0)
        mapping = Mapping.byCode(0)
        dataWrite = 0
    }

    fun write(value: IncrementMode) {
        val a = mapping.map(address)
        if (value == IncrementMode.LOW) {
            vram[a] = dataWrite.lowByte().toByte()
        } else {
            vram[a + 1] = dataWrite.highByte().toByte()
        }

        if (addressIncrementMode == value) {
            address += increment.amount
        }
    }

    fun read(value: IncrementMode): Int {
        val a = mapping.map(address)
        val r = if (value == IncrementMode.LOW) {
            vram[a]
        } else {
            vram[a + 1]
        }

        if (addressIncrementMode == value) {
            address += increment.amount
        }

        return r.toInt()
    }

    enum class Increment(val amount: Int) {
        _1(1),
        _32(2),
        _128(128),
        _128_2(128);

        val code get() = ordinal

        companion object {
            fun byCode(code: Int) = values()[code]
        }
    }

    enum class IncrementMode {
        LOW,
        HIGH
    }

    enum class Mapping {
        NO_MAPPING,
        SIZE_8,
        SIZE_7,
        SIZE_6;

        val code get() = ordinal

        fun map(address: ShortAddress): ShortAddress
                = when (this) {
            NO_MAPPING -> address
            SIZE_8 -> (address and 0xFF00) or (address and 0x001F shl 3) or (address and 0x00E0 shl 5)
            SIZE_7 -> (address and 0xFE00) or (address and 0x003F shl 3) or (address and 0x01C0 shl 6)
            SIZE_6 -> (address and 0xFC00) or (address and 0x007F shl 3) or (address and 0x0380 shl 7)
        }

        companion object {
            fun byCode(code: Int) = values()[code]
        }
    }
}