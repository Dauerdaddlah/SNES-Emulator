package de.dde.snes.processor.register

import de.dde.snes.asByte
import de.dde.snes.isBitSet
import de.dde.snes.setBit

class StatusRegister : Register {
    /**
     * whether this statusRegister should be considered in emulation or native mode.
     * In Emulation mode the memory-flag/-Bit is always set and the Index-flag represents the interrupt "reason", 1 for BRK, 0 else
     */
    var emulationMode = true
        set(value) {
            if (value) {
                memory = true
                index = true
            }
            field = value
        }

    override val size: Int
        get() = 1

    /** C - carry*/
    var carry: Boolean = false
    /** Z - Zero*/
    override var zero: Boolean = false
    /** I - IRQ disable*/
    var irqDisable: Boolean = true
    /** D - desimal mode*/
    var decimal: Boolean = false
    /** X - index register select if true the index register shall work in 8-bit else in 16-bit */
    var index: Boolean = true
    /** M - memory select if true the accumulator shall work in 8-bit, else in 16-bit */
    var memory: Boolean = true
    /** V - Overflow */
    var overflow: Boolean = false
    /** N - Negative */
    override var negative: Boolean = false

    /** B - BRK command */
    var _break: Boolean
        get() = index
        set(value) { index = value }


    override fun reset() {
        carry = false
        zero = false
        irqDisable = true
        decimal = false
        index = true
        memory = true
        overflow = false
        negative = false
    }

    override fun set(value: Int) {
        carry = value.isBitSet(BIT_CARRY)
        zero = value.isBitSet(BIT_ZERO)
        irqDisable = value.isBitSet(BIT_IRQ_DISABLE)
        decimal = value.isBitSet(BIT_DECIMAL)
        if (emulationMode) {
            _break = value.isBitSet(BIT_BREAK)
            memory = true
        } else {
            index = value.isBitSet(BIT_INDEX)
            memory = value.isBitSet(BIT_MEMORY)
        }
        overflow = value.isBitSet(BIT_OVERFLOW)
        negative = value.isBitSet(BIT_NEGATIVE)
    }

    fun setBit(bits: Int, set: Boolean) {
        if (bits.isBitSet(BIT_CARRY)) carry = set
        if (bits.isBitSet(BIT_ZERO)) zero = set
        if (bits.isBitSet(BIT_IRQ_DISABLE)) irqDisable = set
        if (bits.isBitSet(BIT_DECIMAL)) decimal = set
        if (emulationMode) {
            if (bits.isBitSet(BIT_BREAK)) _break = set
        } else {
            if (bits.isBitSet(BIT_INDEX)) index = set
            if (bits.isBitSet(BIT_MEMORY)) memory = set
        }
        if (bits.isBitSet(BIT_OVERFLOW)) overflow = set
        if (bits.isBitSet(BIT_NEGATIVE)) negative = set
    }

    override fun get(): Int {
        var bits = 0
        if (carry) bits = bits.setBit(BIT_CARRY)
        if (zero) bits = bits.setBit(BIT_ZERO)
        if (irqDisable) bits = bits.setBit(BIT_IRQ_DISABLE)
        if (decimal) bits = bits.setBit(BIT_DECIMAL)
        if (emulationMode) { if (_break) bits = bits.setBit(BIT_BREAK) } else if (index) bits = bits.setBit(BIT_INDEX)
        if (emulationMode || memory) bits = bits.setBit(BIT_MEMORY)
        if (overflow) bits = bits.setBit(BIT_OVERFLOW)
        if (negative) bits = bits.setBit(BIT_NEGATIVE)
        return bits
    }

    override fun trimValue(value: Int): Int {
        return value.asByte()
    }

    override fun isNegative(value: Int): Boolean {
        return value.isBitSet(0x80)
    }

    override fun isZero(value: Int): Boolean {
        return trimValue(value) == 0
    }

    companion object {
        const val BIT_CARRY = 0x01
        const val BIT_ZERO = 0x02
        const val BIT_IRQ_DISABLE = 0x04
        const val BIT_DECIMAL = 0x08
        const val BIT_INDEX = 0x10
        const val BIT_MEMORY = 0x20
        const val BIT_OVERFLOW = 0x40
        const val BIT_NEGATIVE = 0x80

        const val BIT_BREAK = BIT_INDEX
    }
}