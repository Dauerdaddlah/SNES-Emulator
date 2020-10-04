package de.dde.snes.processor

import de.dde.snes.*
import de.dde.snes.memory.*
import de.dde.snes.processor.addressmode.*
import de.dde.snes.processor.instruction.Instruction
import de.dde.snes.processor.register.*
import java.nio.file.Files
import java.nio.file.Paths

class Processor(
    val memory: Memory
) {
    val chipVersion: Int = 2

    val wram = WRAM()

    /** if xirq is enabled and hcounter reaches this value an irq is fired (yirq is considered as well) */
    var htime = 0x1FF
    /** if yirq is enabled and vcounter reaches this value an irq is fired (xirq is considered as well) */
    var vtime = 0x1FF

    data class Multiplication(
        var multiplicantA: Int = 0xFF,
        var multiplicantB: Int = 0,
        var product: Int = 0
    )
    val multiplication = Multiplication()

    data class Division(
        var divident: Int = 0xFFFF,
        var divisor: Int = 0,
        var quotient: Int = 0,
        var remainder: Int = 0
    )
    val division = Division()

    var multiplicationDone = false

    /** if true, access to a certain region in memory will be faster than normal */
    var fastRom = false

    var nmiEnabled = false
    var nmiRequested = false
    /** flag is set on nmi and reset on reading */
    var nmiFlag = false

    var yIrqEnabled = false
    var xIrqEnabled = false
    var irqRequested = false
    /** flag set if an irq occurred and reset on reading */
    var irqFlag = false

    var autoJoypadRead = false

    // TODO consider cycles for everything else than memory-access

    var mode = ProcessorMode.EMULATION

    // registers
    /** accumulator */
    val rA = Accumulator()
    /** Data Bank Register */
    val rDBR = Register8Bit()
    /** Direct */
    val rD = Register16Bit()
    /** X Index Register */
    val rX = Register8Bit16Bit()
    /** Y Index Register */
    val rY = Register8Bit16Bit()
    /** Processor Status Register */
    val rP = StatusRegister()
    /** Program Bank Register */
    val rPBR = Register8Bit()
    /** Program Counter */
    val rPC = Register16Bit()
    /** Stack Pointer */
    val rS = StackPointer()

    internal val operations = Operations()
    internal val addressModes = AddressModes()
    internal val instructions = Instructions()
    private val instData = InstructionData()

    var waitForInterrupt = false
        private set

    var cycles = 0L
        private set
    var instructionCount = 0L
        private set

    fun reset() {
        wram.reset()

        nmiEnabled = false
        yIrqEnabled = false
        xIrqEnabled = false
        autoJoypadRead = false

        htime = 0x1FF
        vtime = 0x1FF

        with (multiplication) {
            multiplicantA = 0xFF
            multiplicantB = 0
            product = 0
        }

        with (division) {
            divident = 0xFFFF
            divisor = 0
            quotient = 0
            remainder = 0
        }

        multiplicationDone = false

        fastRom = false

        mode = ProcessorMode.EMULATION
        rP.reset()

        rA.reset()
        rDBR.reset()
        rD.reset()
        rX.reset()
        rY.reset()
        rPBR.reset()
        rS.reset()

        waitForInterrupt = false
        cycles = 0
        instructionCount = 0

        rPC.value = readShort(
            0,
            EMULATION_RESET_VECTOR_ADDRESS
        )

        checkRegisterSizes()
    }

    fun checkRegisterSizes() {
        rP.emulationMode = mode == ProcessorMode.EMULATION
        rA.size16Bit = mode == ProcessorMode.NATIVE && !rP.memory
        rX.size16Bit = mode == ProcessorMode.NATIVE && !rP.index
        rY.size16Bit = rX.size16Bit
        rS.size16Bit = mode == ProcessorMode.NATIVE
    }

    val log = SnesLog("new", 1000000, false)

    fun executeNextInstruction() {
        // TODO correct cycles
        // add 1 cycle no matter the action
        cycles++

        when {
            nmiRequested -> {
                NMI()
            }
            irqRequested -> {
                IRQ()
            }
            waitForInterrupt -> {
                return
            }
            else -> {
                log.prepare(rA.getFull(), rX.get(), rY.get(), rS.get(), rD.get(), rP.get(), rPC.get(), rDBR.get(), rPBR.get(), mode == ProcessorMode.EMULATION)

                val i = fetch()
                val inst = instructions[i]

                instructionCount++

                execute(inst)

                log.log(instData.inst.operation.symbol, fullAddress(instData.bank, instData.address), instData.value)
            }
        }
    }

    fun execute(inst: Instruction) {
        instData.updateInst(inst)

        inst.operation.execute()
    }

    private fun fetch(): Int {
        val v = readByteInt(
            rPBR.value,
            rPC.value
        )
        rPC.inc()
        return v
    }

    private fun fetchShort(): Int
        = Short(fetch(), fetch())

    private fun fetchLong(): Int
        = fetchShort().withLongByte(fetch())

    private fun NMI() {
        nmiRequested = false

        if (!nmiEnabled) {
            return
        }
        nmiFlag = true
        interrupt(EMULATION_NMI_VECTOR_ADDRESS, NATIVE_NMI_VECTOR_ADDRESS, false)
    }

    private fun IRQ() {
        irqRequested = false

        if (rP.irqDisable) {
            return
        }
        irqFlag = true
        interrupt(EMULATION_IRQ_VECTOR_ADDRESS, NATIVE_IRQ_VECTOR_ADDRESS, false)
    }

    private fun readByteInt(bank: Bank, address: ShortAddress): Int {
        cycles += getCyclesForAddress(bank, address)
        return memory.readByte(bank, address)
    }

    private fun readShort(bank: Bank, address: ShortAddress)
        = Short(readByteInt(bank, address), (readByteInt(bank, shortAddress(address.shortAddress + 1))))

    private fun readLong(bank: Bank, address: ShortAddress)
        = readShort(bank, address).withLongByte(readByteInt(bank, shortAddress(address.shortAddress + 2)))

    private fun writeByteInt(bank: Bank, address: ShortAddress, value: Int) {
        cycles += getCyclesForAddress(bank, address)
        memory.writeByte(bank, address, value)
    }

    private fun writeWord(bank: Bank, address: ShortAddress, value: Int) {
        writeByteInt(bank, address, value)
        writeByteInt(bank, shortAddress(address.shortAddress + 1), value shr 8)
    }

    private fun getCyclesForAddress(bank: Bank, address: ShortAddress): Int {
        // TODO copied from snes9x
        if (bank and 0x40 != 0 || address and 0x8000 != 0) {
            return if (bank and 0x80 != 0) (if (fastRom) ONE_CYCLE else SLOW_ONE_CYCLE) else SLOW_ONE_CYCLE
        }

        if (address + 0x6000 and 0x4000 != 0) return SLOW_ONE_CYCLE

        return if (address - 0x4000 and 0x7e00 != 0) ONE_CYCLE else TWO_CYCLES

    }

    private fun pushByte(value: Int) {
        writeByteInt(
            0,
            rS.get(),
            value)
        rS.dec()
    }

    private fun pushShort(value: Int) {
        pushByte(value shr 8)
        pushByte(value)
    }

    private fun pullByte(): Int {
        rS.inc()
        return readByteInt(
            0,
            rS.get()
        )
    }

    private fun pullShort(): Int {
        return pullByte() + (pullByte() shl 8)
    }

    fun interrupt(addressEmulation: ShortAddress, addressNative: ShortAddress, adjustPC: Boolean = true) {
        val interruptAddress = if (mode == ProcessorMode.EMULATION) addressEmulation else addressNative
        waitForInterrupt = false

        if (adjustPC) {
            // this happens in BRK & COP - as they have a additional description-byte, we need to skip before pushing PC
            rPC.inc()
        }

        if (mode == ProcessorMode.NATIVE) {
            pushByte(rPBR.value)
        }

        pushShort(rPC.value)
        pushByte(rP.get())

        rP.decimal = false
        rP.irqDisable = true

        if (mode == ProcessorMode.NATIVE) {
            rPBR.value = 0
        }

        rPC.value = readShort(0, interruptAddress)
    }

    fun addCarry(r: Register, value1: Int, value2: Int): Int {
        val ret = if (r.size == 1) Algorithms.adc8(value1, rP.get(), value2)
                                else Algorithms.adc16(value1, rP.get(), value2)

        rP.set(ret.second)
        return ret.first
    }

    fun subtractCarry(r: Register, value1: Int, value2: Int): Int {
        val ret = if (r.size == 1) Algorithms.sbc8(value1, rP.get(), value2)
                        else Algorithms.sbc16(value1, rP.get(), value2)

        rP.set(ret.second)
        return ret.first
    }

    // TODO replace by own implementation
    // adapted from algorithms.cpp
    object Algorithms {
        // These assume that host machine uses two's complement

        private const val n80 = 0x80
        private const val v40 = 0x40
        private const val d08 = 0x08
        private const val z02 = 0x02
        private const val c01 = 0x01

        fun adc8( a: Int, pStart: Int, operand: Int ): Pair<Int, Int>
        {
            var p = pStart
            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x0F)+(operand and 0x0F)+carry
                if (result > 9)
                    result += 6

                carry = if(result > 0x0F) 1 else 0
                result = (a and 0xF0)+(operand and 0xF0)+(result and 0x0F)+(carry * 0x10)
            }

            // signs of a and operand match, and sign of result doesn't
            if ((a and 0x80) == (operand and 0x80) && (a and 0x80) != (result and 0x80))
                p = p or v40

            if ((p and d08) != 0 && result > 0x9F)
                result += 0x60

            if (result > 0xFF)
                p = p or c01

            if (result and 0x80 != 0)
                p = p or n80

            if ((result and 0xFF) == 0)
                p = p or z02

            return (result and 0xFF) to p
        }

        fun sbc8( a: Int, pStart: Int, operandStart: Int ): Pair<Int, Int>
        {
            var p = pStart
            var operand = operandStart

            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            operand = operand xor 0xFF

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x0F)+(operand and 0x0F)+carry
                if (result < 0x10)
                    result -= 6

                carry = if(result > 0x0F) 1 else 0
                result = (a and 0xF0)+(operand and 0xF0)+(result and 0x0F)+(carry * 0x10)
            }

            // signs of a and operand match, and sign of result doesn't
            if ((a and 0x80) == (operand and 0x80) && (a and 0x80) != (result and 0x80))
                p = p or v40

            if ((p and d08) != 0 && result < 0x100)
                result -= 0x60

            if (result > 0xFF)
                p = p or c01

            if ((result and 0x80) != 0)
                p = p or n80

            if ((result and 0xFF) == 0)
                p = p or z02

            return (result and 0xFF) to p
        }

        fun adc16( a: Int, pStart: Int, operand: Int ): Pair<Int, Int>
        {
            var p = pStart

            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x000F)+(operand and 0x000F)+carry
                if (result > 0x0009)
                    result += 0x0006

                carry = if(result > 0x000F) 1 else 0

                result = (a and 0x00F0)+(operand and 0x00F0)+(result and 0x000F)+carry * 0x10
                if (result > 0x009F)
                    result += 0x0060

                carry = if(result > 0x00FF) 1 else 0

                result = (a and 0x0F00)+(operand and 0x0F00)+(result and 0x00FF)+carry * 0x100
                if (result > 0x09FF)
                    result += 0x0600

                carry = if(result > 0x0FFF) 1 else 0

                result = (a and 0xF000)+(operand and 0xF000)+(result and 0x0FFF)+carry * 0x1000
            }

            // signs of a and operand match, and sign of result doesn't
            if ((a and 0x8000) == (operand and 0x8000) && (a and 0x8000) != (result and 0x8000))
                p = p or v40

            if ((p and d08) != 0 && result > 0x9FFF)
                result += 0x6000

            if (result > 0xFFFF)
                p = p or c01

            if (result and 0x8000 != 0)
                p = p or n80

            if ((result and 0xFFFF) == 0)
                p = p or z02

            return (result and 0xFFFF) to p
        }

        fun sbc16( a: Int, pStart: Int, operandStart: Int ): Pair<Int, Int>
        {
            var p = pStart

            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            val operand = operandStart xor 0xFFFF

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x000F)+(operand and 0x000F)+carry
                if (result < 0x0010)
                    result -= 0x0006

                carry = if(result > 0x000F) 1 else 0

                result = (a and 0x00F0)+(operand and 0x00F0)+(result and 0x000F)+carry * 0x10
                if (result < 0x0100)
                    result -= 0x0060

                carry = if(result > 0x00FF) 1 else 0

                result = (a and 0x0F00)+(operand and 0x0F00)+(result and 0x00FF)+carry * 0x100
                if (result < 0x1000)
                    result -= 0x0600

                carry = if(result > 0x0FFF) 1 else 0

                result = (a and 0xF000)+(operand and 0xF000)+(result and 0x0FFF)+carry * 0x1000
            }

            // signs of addends match, and sign of result doesn't
            if (((a xor operand) and 0x8000) == 0 && ((a xor result) and 0x8000) != 0)
                p = p or v40

            if ((p and d08) != 0 && result < 0x10000)
                result -= 0x6000

            if (result > 0xFFFF)
                p = p or c01

            if (result and 0x8000 != 0)
                p = p or n80

            if ((result and 0xFFFF) == 0)
                p = p or z02

            return (result and 0xFFFF) to p
        }
    }

    fun and(r: Register, value1: Int, value2: Int): Int {
        val result = r.trimValue(value1 and value2)
        rP.negative = r.isNegative(result)
        rP.zero = result == 0
        return result
    }

    fun eor(r: Register, value1: Int, value2: Int): Int {
        val result = r.trimValue(value1 xor value2)
        rP.negative = r.isNegative(result)
        rP.zero = result == 0
        return result
    }

    fun or(r: Register, value1: Int, value2: Int): Int {
        val result = r.trimValue(value1 or value2)
        rP.negative = r.isNegative(result)
        rP.zero = result == 0
        return result
    }

    fun compare(r: Register, value1: Int, value2: Int) {
        val result = value1 - value2
        rP.carry = value1 >= value2
        rP.zero = r.trimValue(result) == 0
        rP.negative = r.isNegative(result)
    }

    fun bitTest(r: Register, value1: Int, value2: Int, onlyZero: Boolean = false) {
        val result = value1 and value2
        rP.zero = result == 0
        if (!onlyZero) {
            rP.negative = r.isNegative(value2)
            rP.overflow = if (r.size == 1) value2.isBitSet(0x40) else value2.isBitSet(0x4000)
        }
    }

    fun shiftLeft(r: Register, value: Int, withCarry: Boolean): Int {
        val result = value shl 1 or (if (withCarry && rP.carry) 1 else 0)
        val v = r.trimValue(result)
        rP.negative = r.isNegative(v)
        rP.zero = v == 0
        rP.carry = result != v
        return v
    }

    fun shiftRight(r: Register, value: Int, withCarry: Boolean): Int {
        val result = value shr 1 or (if (!withCarry || ! rP.carry) 0 else if (r.size == 1) 0x80 else 0x8000)
        rP.negative = r.isNegative(result)
        rP.zero = result == 0
        rP.carry = value.isBitSet(1)
        return result
    }

    fun add(r: Register, value: Int, toAdd: Int): Int {
        val result = r.trimValue(value + toAdd)
        rP.negative = r.isNegative(result)
        rP.zero = result == 0
        return result
    }

    fun branch(bank: Bank, address: ShortAddress, withBank: Boolean = false) {
        rPC.value = address
        if (withBank) {
            rPBR.value = bank
        }
    }

    fun jump(bank: Bank, address: ShortAddress, withBank: Boolean = false) {
        if (withBank) {
            pushByte(rPBR.value)
        }
        pushShort(rPC.value - 1)
        rPC.value = address
        if (withBank) {
            rPBR.value = bank
        }
    }

    fun setPBits(bits: Int, set: Boolean) {
        rP.setBit(bits, set)

        checkRegisterSizes()
    }

    fun testAndSetBits(r: Register, bits: Int, set: Boolean): Int {
        rP.zero = r.trimValue(r.get() and bits) == 0
        return if (set) {
            r.trimValue(r.get() or bits)
        } else {
            r.trimValue(bits and r.get().inv())
        }
    }

    fun transfer(rFrom: Register, rTo: Register, full: Boolean = false) {
        val resizeFrom = (full || rTo.size == 2) && rFrom.size == 1
        val resizeTo = full && rTo.size == 1

        if (resizeFrom && rFrom is Register8Bit16Bit) {
            rFrom.size16Bit = true
        }

        if (resizeTo && rTo is Register8Bit16Bit) {
            rTo.size16Bit = true
        }

        rTo.set(rFrom.get())

        if (rTo != rS) {
            rP.negative = rTo.negative
            rP.zero = rTo.zero
        }

        if (resizeFrom && rFrom is Register8Bit16Bit) {
            rFrom.size16Bit = false
        }

        if (resizeTo && rTo is Register8Bit16Bit) {
            rTo.size16Bit = false
        }
    }

    private fun pullStack(r: Register) {
        r.set(pullStack(r.size))

        if (r !is StackPointer) {
            rP.zero = r.zero
            rP.negative = r.negative
        }
    }

    fun pullStack(size: Int): Int {
        return when (size) {
            1 -> pullByte()
            2 -> pullShort()
            else -> error("invalid size<$size> - can only pull 1 or 2 bytes at a time")
        }
    }

    private fun pushStack(r: Register) {
        pushStack(r.get(), r.size)
    }

    fun pushStack(value: Int, size: Int) {
        when (size) {
            1 -> pushByte(value)
            2 -> pushShort(value)
            else -> error("invalid size<$size> - can only push 1 or 2 bytes at a time")
        }
    }

    fun xba() {
        rA.xba()
        val size16 = rA.size16Bit

        rA.size16Bit = false
        rP.zero = rA.zero
        rP.negative = rA.negative
        rA.size16Bit = size16
    }

    fun xce() {
        val t = rP.carry
        rP.carry = mode == ProcessorMode.EMULATION
        mode = if (t) ProcessorMode.EMULATION else ProcessorMode.NATIVE

        checkRegisterSizes()
    }

    fun waitForInterrupt() {
        println("WAI")
        waitForInterrupt = true
    }

    private fun write(bank: Bank, address: ShortAddress, r: Register) {
        write(bank, address, r.get(), r.size)
    }

    fun write(bank: Bank, address: ShortAddress, value: Int, size: Int = 1) {
        when (size) {
            1 -> writeByteInt(bank, address, value)
            2 -> writeWord(bank, address, value)
        }
    }

    private fun read(bank: Bank, address: ShortAddress, size: Int = 1): Int {
        return when (size) {
            1 -> readByteInt(bank, address)
            2 -> readShort(bank, address)
            else -> error("can only read 1 or 2 bytes")
        }
    }

    fun load(r: Register, value: Int) {
        r.set(value)
        rP.negative = r.negative
        rP.zero = r.zero
    }

    fun blockMove(bankDest: Bank, bankSource: Bank, increment: Int) {
        rDBR.value = bankDest

        val sizeA = rA.size16Bit
        // A is always considered 16-bit in block-move-instructions
        rA.size16Bit = true

        while (!rA.zero) {
            writeByteInt(bankDest, rY.get(),
                readByteInt(bankSource, rX.get()))

            rA.set(rA.get() - 1)
            rX.set(rX.get() + increment)
            rY.set(rY.get() + increment)
        }

        writeByteInt(bankDest, rY.get(),
            readByteInt(bankSource, rX.get()))

        rA.set(rA.get() - 1)
        rX.set(rX.get() + increment)
        rY.set(rY.get() + increment)

        rA.size16Bit = sizeA
    }

    fun returnFromInterrupt() {
        rP.set(pullByte())

        checkRegisterSizes()

        rPC.value = pullShort()

        if (mode == ProcessorMode.NATIVE) {
            rPBR.value = pullByte()
        }
    }

    fun returnFromSubroutine(withBank: Boolean) {
        rPC.value = pullShort()
        if (withBank) {
            rPBR.value = pullByte()
        }
        rPC.inc()
    }

    internal inner class AddressModes {
        /** a */       val absolute = AddressModeSimple("a", "Absolute", AddressModeResult.ADDRESS_DBR) { fetchShort() }
        /** (a,x) */   val absoluteIndexedIndirect = addressModeIndirectShort("(a,x)", "Absolute Indexed Indirect", { it + rX.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.SHORTADDRESS)
        /** a,x */     val absoluteIndexedWithX = addressModeDbrShort("a,x", "Absolute Indexed With X", rX)
        /** a,y */     val absoluteIndexedWithY = addressModeDbrShort("a,y", "Absolute Indexed With Y", rY)
        /** (a) */     val absoluteIndirect = addressModeIndirectShort("(a)", "Absolute Indirect", { it }, AddressModeResult.ADDRESS_0, AddressModeResult.SHORTADDRESS)
        /** (a) */     val absoluteIndirectLong = addressModeIndirectShort("(a)", "Absolute Indirect", {it }, AddressModeResult.ADDRESS_0, AddressModeResult.FULLADDRESS)
        /** al,x */    val absoluteLongIndexedWithX = AddressModeSimple("al,x", "Absolute Long Indexed With X", AddressModeResult.FULLADDRESS) { fetchLong() + rX.get() }
        /** al */      val absoluteLong = AddressModeSimple("al", "Absolute Long", AddressModeResult.FULLADDRESS) { fetchLong() }
        /** (d,x) */   val directIndexedIndirect = addressModeIndirect("(d,x)", "Direct Indexed Indirect", { it + rD.get() + rX.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.ADDRESS_DBR)
        /** d,x */     val directIndexedWithX = addressMode0("d,x", "Direct Indexed With X", rD, rX)
        /** d,y */     val directIndexedWithY = addressMode0("d,y", "Direct Indexed With Y", rD, rY)
        /** (d),y */   val directIndirectIndexed = addressModeIndirect("(d),y", "Direct Indirect Indexed", { it + rD.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.ADDRESS_DBR, { it + rY.get() }, AddressModeResult.FULLADDRESS)
        /** \[d],y */  val directIndirectLongIndexed = addressModeIndirect("[d],y", "Direct Indirect Long Indexed", { it + rD.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.FULLADDRESS, { it + rY.get() }, AddressModeResult.FULLADDRESS)
        /** \[d] */    val directIndirectLong = addressModeIndirect("[d]", "Direct Indirect Long", { it + rD.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.FULLADDRESS)
        /** (d) */     val directIndirect = addressModeIndirect("(d)", "Direct Indirect", { it + rD.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.ADDRESS_DBR)
        /** d */       val direct = addressMode0("d", "Direct", rD)
        /** # */       val immediate = AddressModeSimple("#", "Immediate", AddressModeResult.IMMEDIATE) { fetch() }
        /** rl */      val programCounterRelativeLong: AddressMode = AddressModeSimple("rl", "Program Counter Relative Long", AddressModeResult.SHORTADDRESS) { fetchShort().toShort().toInt() + rPC.get() }
        /** r */       val programCounterRelative: AddressMode = AddressModeSimple("r", "Program Counter Relative", AddressModeResult.SHORTADDRESS) { fetch().toByte().toInt() + rPC.get() }
        /** d,s */     val stackRelative = addressMode0("d,s", "Stack Relative", rS)
        /** (d,s),y */ val stackRelativeIndirectIndexed = addressModeIndirect("(d,s),y", "Stack Relative Indirect Indexed", { it + rS.get() }, AddressModeResult.ADDRESS_0, AddressModeResult.ADDRESS_DBR, { it + rY.get() }, AddressModeResult.FULLADDRESS)
        /** A */       val accumulator = AddressModeSimple("A", "Accumulator", AddressModeResult.ACCUMULATOR) { rA.get() }
        /** xyc */     val blockMove = AddressModeSimple("xyc", "Block Move", AddressModeResult.IMMEDIATE) { fetch() }
        /** i */       val implied = noAddressMode("i", "Implied", "Implied does not provide any value")
        /** s */       val stack = noAddressMode("s", "Stack", "Stack does not fetch any value")
        /**  */        val addressNull = noAddressMode("", "", "no valid addressMode")

        private fun noAddressMode(
            symbol: String,
            description: String,
            errorMessage: String
        ): AddressMode = AddressModeNoValue(symbol, description, AddressModeResult.NOTHING, errorMessage)

        private fun addressMode0(
            symbol: String,
            description: String,
            r: Register
        ): AddressMode = AddressModeSimple(
            symbol,
            description,
            AddressModeResult.ADDRESS_0
        ) { shortAddress(fetch() + r.get())}

        private fun addressMode0(
            symbol: String,
            description: String,
            r: Register,
            r2: Register
        ): AddressMode = AddressModeSimple(
            symbol,
            description,
            AddressModeResult.ADDRESS_0
        ) { shortAddress(fetch() + r.get() + r2.get())}

        private fun addressModeDbrShort(
            symbol: String,
            description: String,
            r: Register
        ): AddressMode = AddressModeSimple(
            symbol,
            description,
            AddressModeResult.ADDRESS_DBR
        ) { shortAddress(fetchShort() + r.get())}

        private fun addressModeIndirect(
            symbol: String,
            description: String,
            prepareAddress1: (Int) -> Int,
            middleResult: AddressModeResult,
            middleResult2: AddressModeResult,
            prepareAddress2: (Int) -> Int = { it },
            endResult: AddressModeResult = middleResult2
        ): AddressMode = AddressModeSimple(
            symbol,
            description,
            endResult
        ) {
            var r = fetch()

            r = prepareAddress1(r)

            val bank1 = when (middleResult) {
                AddressModeResult.SHORTADDRESS,
                AddressModeResult.ADDRESS_PBR -> rPBR.get()
                AddressModeResult.FULLADDRESS -> {
                    val b = r.longByte()
                    r = r.shortAddress
                    b
                }
                AddressModeResult.ADDRESS_0 -> 0
                AddressModeResult.ADDRESS_DBR -> rDBR.get()
                else -> error("AddressMode<$middleResult> not allowed as middleResult for indirect addressmodes")
            }

            r = when (middleResult2.size) {
                1 -> readByteInt(bank1, r)
                2 -> readShort(bank1, r)
                3 -> readLong(bank1, r)
                else -> error("invalid middleResult<$middleResult2> for indirect addressmode")
            }

            val bank2 = when (middleResult2) {
                AddressModeResult.SHORTADDRESS,
                AddressModeResult.ADDRESS_PBR -> rPBR.get()
                AddressModeResult.FULLADDRESS -> {
                    val b = r.longByte()
                    r = r.shortAddress
                    b
                }
                AddressModeResult.ADDRESS_0 -> 0
                AddressModeResult.ADDRESS_DBR -> rDBR.get()
                else -> error("AddressMode<$middleResult> not allowed as middleResult for indirect addressmodes")
            }

            if (endResult.size == 3) {
                r = r.withLongByte(bank2)
            }

            r = prepareAddress2(r)

            r
        }

        private fun addressModeIndirectShort(
            symbol: String,
            description: String,
            prepareAddress1: (Int) -> Int,
            middleResult: AddressModeResult,
            middleResult2: AddressModeResult,
            prepareAddress2: (Int) -> Int = { it },
            endResult: AddressModeResult = middleResult2
        ): AddressMode = AddressModeSimple(
            symbol,
            description,
            endResult
        ) {
            var r = fetchShort()

            r = prepareAddress1(r)

            val bank1 = when (middleResult) {
                AddressModeResult.SHORTADDRESS,
                AddressModeResult.ADDRESS_PBR -> rPBR.get()
                AddressModeResult.FULLADDRESS -> {
                    val b = r.longByte()
                    r = r.shortAddress
                    b
                }
                AddressModeResult.ADDRESS_0 -> 0
                AddressModeResult.ADDRESS_DBR -> rDBR.get()
                else -> error("AddressMode<$middleResult> not allowed as middleResult for indirect addressmodes")
            }

            r = when (middleResult2.size) {
                1 -> readByteInt(bank1, r)
                2 -> readShort(bank1, r)
                3 -> readLong(bank1, r)
                else -> error("invalid middleResult<$middleResult2> for indirect addressmode")
            }

            val bank2 = when (middleResult2) {
                AddressModeResult.SHORTADDRESS,
                AddressModeResult.ADDRESS_PBR -> rPBR.get()
                AddressModeResult.FULLADDRESS -> {
                    val b = r.longByte()
                    r = r.shortAddress
                    b
                }
                AddressModeResult.ADDRESS_0 -> 0
                AddressModeResult.ADDRESS_DBR -> rDBR.get()
                else -> error("AddressMode<$middleResult> not allowed as middleResult for indirect addressmodes")
            }

            if (endResult.size == 3) {
                r = r.withLongByte(bank2)
            }

            r = prepareAddress2(r)

            r
        }
    }

    inner class Operations {
        val adc = OperationRead("ADC", "add with carry", rA) { value -> rA.set(addCarry(rA, rA.get(), value)) }
        val and = OperationRead("AND", "And with accumulator", rA) { value -> rA.set(and(rA, rA.get(), value)) }
        val asl = OperationSet("ASL", "Shift left", rA) { value -> shiftLeft(rA, value, false) }
        val bcc = OperationAddress("BCC", "Branch if carry clear") { bank, address -> if (!rP.carry) branch(bank, address) }
        val bcs = OperationAddress("BCS", "Branch if carry set") { bank, address -> if (rP.carry) branch(bank, address) }
        val beq = OperationAddress("BEQ", "Branch if Equal/Zero set") { bank, address -> if (rP.zero) branch(bank, address) }
        val bit = OperationRead("BIT", "Bit test between A and the value", rA) { v -> bitTest(rA, rA.get(), v, instData.result == AddressModeResult.IMMEDIATE) }
        val bmi = OperationAddress("BMI", "Branch if Minus/Negative set") { bank, address -> if (rP.negative) branch(bank, address) }
        val bne = OperationAddress("BNE", "Branch if Not equal/zero clear") { bank, address -> if (!rP.zero) branch(bank, address) }
        val bpl = OperationAddress("BPL", "Branch if Plus/negative clear") { bank, address -> if (!rP.negative) branch(bank, address)}
        val bra = OperationAddress("BRA", "Branch Always") { bank, address -> branch(bank, address) }
        val brk = OperationSimple0("BRK", "Force Break") { interrupt(EMULATION_BRK_VECTOR_ADDRESS, NATIVE_BRK_VECTOR_ADDRESS) }
        val brl = OperationAddress("BRL", "Branch Always Long") { bank, address -> branch(bank, address, true) }
        val bvc = OperationAddress("BVC", "Branch if Overflow clear") { bank, address -> if (!rP.overflow) branch(bank, address) }
        val bvs = OperationAddress("BVS", "Branch If Overflow set") { bank, address -> if (rP.overflow) branch(bank, address) }
        val clc = OperationSimple0("CLC", "Clear carry flag") { setPBits(StatusRegister.BIT_CARRY, false) }
        val cld = OperationSimple0("CLD", "Clear decimal flag") { setPBits(StatusRegister.BIT_DECIMAL, false) }
        val cli = OperationSimple0("CLI", "Clear irq/interrupt flag") { setPBits(StatusRegister.BIT_IRQ_DISABLE, false) }
        val clv = OperationSimple0("CLV", "Clear Overflow flag") { setPBits(StatusRegister.BIT_OVERFLOW, false) }
        val cmp = OperationRead("CMP", "Compare value with A", rA) { value -> compare(rA, rA.get(), value) }
        val cop = OperationSimple0("COP", "Coprocessor interrupt") { interrupt(EMULATION_COP_VECTOR_ADDRESS, NATIVE_COP_VECTOR_ADDRESS) }
        val cpx = OperationRead("CPX", "Compare value with X", rX) { value -> compare(rX, rX.get(), value) }
        val cpy = OperationRead("CPY", "Compare value with Y", rY) { value -> compare(rY, rY.get(), value) }
        val dec = OperationSet("DEC", "Decrement Accumulator or Address", rA) { value -> add(rA, value, -1) }
        val dex = OperationSimple0("DEX", "Decrement X") { rX.set(add(rX, rX.get(), -1)) }
        val dey = OperationSimple0("DEY", "Decrement Y") { rY.set(add(rY, rY.get(), -1)) }
        val eor = OperationRead("EOR", "Exclusive or with accumulator", rA) { value -> rA.set(eor(rA, rA.get(), value)) }
        val inc = OperationSet("INC", "Increment Accumulator or Address", rA) { value -> add(rA, value, 1) }
        val inx = OperationSimple0("INX", "Increment X") { rX.set(add(rX, rX.get(), 1)) }
        val iny = OperationSimple0("INY", "Increment Y") { rY.set(add(rY, rY.get(), 1)) }
        val jml = OperationAddress("JML", "Jump to address long") { bank, address -> branch(bank, address, true) }
        val jmp = OperationAddress("JMP", "Jump to address") { bank, address -> branch(bank, address, false) }
        val jsl = OperationAddress("JSL", "Jump to subroutine long") { bank, address -> jump(bank, address, true) }
        val jsr = OperationAddress("JSR", "Jump to subroutine") { bank, address -> jump(bank, address) }
        val lda = OperationRead("LDA", "Load A", rA) { value -> load(rA, value) }
        val ldx = OperationRead("LDX", "Load X", rX) { value -> load(rX, value) }
        val ldy = OperationRead("LDY", "Load Y", rY) { value -> load(rY, value) }
        val lsr = OperationSet("LSR", "Shift right", rA) { value -> shiftRight(rA, value, false) }
        val mvn = OperationSimple0("MVN", "Block move negative") { with(instData.inst.addressMode) { blockMove(fetchValue(), fetchValue(), -1) } }
        val mvp = OperationSimple0("MVP", "Block move positive") { with(instData.inst.addressMode) { blockMove(fetchValue(), fetchValue(), 1) } }
        val nop = OperationSimple0("NOP", "No operation") { }
        val ora = OperationRead("ORA", "Or with accumulator", rA) { value -> rA.set(or(rA, rA.get(), value)) }
        val pea = OperationAddress("PEA", "Push effective address") { _, address -> pushStack(address, 2)}
        val pei = OperationAddress("PEI", "Push effective indirect address") { _, address -> pushStack(address, 2)}
        val per = OperationAddress("PER", "Push relative address") { _, address -> pushStack(address, 2)}
        val pha = OperationSimple0("PHA", "Push A") { pushStack(rA) }
        val phb = OperationSimple0("PHB", "Push DBR") { pushStack(rDBR) }
        val phd = OperationSimple0("PHD", "Push D") { pushStack(rD) }
        val phk = OperationSimple0("PHK", "Push PBR") { pushStack(rPBR) }
        val php = OperationSimple0("PHP", "Push P") { pushStack(rP) }
        val phx = OperationSimple0("PHX", "Push X") { pushStack(rX) }
        val phy = OperationSimple0("PHY", "Push Y") { pushStack(rY) }
        val pla = OperationSimple0("PLA", "Pull A") { pullStack(rA) }
        val plb = OperationSimple0("PLB", "Pull DBR") { pullStack(rDBR) }
        val pld = OperationSimple0("PLD", "Pull D") { pullStack(rD) }
        val plp = OperationSimple0("PLP", "Pull P") { pullStack(rP) }
        val plx = OperationSimple0("PLX", "Pull X") { pullStack(rX) }
        val ply = OperationSimple0("PLY", "Pull Y") { pullStack(rY) }
        val rep = OperationRead("REP", "Reset Bit in P", rP) { setPBits(it, false) }
        val rol = OperationSet("ROL", "Shift left with carry", rA) { value -> shiftLeft(rA, value, true) }
        val ror = OperationSet("ROR", "Shift right with carry", rA) { value -> shiftRight(rA, value, true) }
        val rti = OperationSimple0("RTI", "Return from Interrupt") { returnFromInterrupt() }
        val rtl = OperationSimple0("RTL", "Return from Subroutine long") { returnFromSubroutine(true) }
        val rts = OperationSimple0("RTS", "Return from Subroutine") { returnFromSubroutine(false) }
        val sbc = OperationRead("SBC", "subtract with carry", rA) { value -> rA.set(subtractCarry(rA, rA.get(), value)) }
        val sep = OperationRead("SEP", "Set Bit in P", rP) { setPBits(it, true) }
        val sec = OperationSimple0("SEC", "Set carry flag") { setPBits(StatusRegister.BIT_CARRY, true) }
        val sed = OperationSimple0("SED", "Set decimal flag") { setPBits(StatusRegister.BIT_DECIMAL, true) }
        val sei = OperationSimple0("SEI", "Set irq/interrupt flag") { setPBits(StatusRegister.BIT_IRQ_DISABLE, true) }
        val sta = OperationAddress("STA", "Store A") { bank, address -> write(bank, address, rA) }
        val stp = OperationSimple0("STP", "Stop the clock") { TODO() }
        val stx = OperationAddress("STX", "Store X") { bank, address -> write(bank, address, rX) }
        val sty = OperationAddress("STY", "Store Y") { bank, address -> write(bank, address, rY) }
        val stz = OperationAddress("STZ", "Store Zero") { bank, address -> write(bank, address, 0, rA.size) }
        val tax = OperationSimple0("TAX", "Transfer A to X") { transfer(rA, rX) }
        val tay = OperationSimple0("TAY", "Transfer A to Y") { transfer(rA, rY) }
        val tcd = OperationSimple0("TCD", "Transfer A to D") { transfer(rA, rD, true) }
        val tcs = OperationSimple0("TCS", "Transfer A to S") { transfer(rA, rS, true) }
        val tdc = OperationSimple0("TDC", "Transfer D to A") { transfer(rD, rA, true) }
        val trb = OperationSet("TRB", "Test and Reset Bits", rA) { value -> testAndSetBits(rA, value, false) }
        val tsb = OperationSet("TSB", "Test and Set Bits", rA) { value -> testAndSetBits(rA, value, true) }
        val tsc = OperationSimple0("TSC", "Transfer S to A") { transfer(rS, rA, true) }
        val tsx = OperationSimple0("TSX", "Transfer S to X") { transfer(rS, rX) }
        val txa = OperationSimple0("TXA", "Transfer X to A") { transfer(rX, rA) }
        val txs = OperationSimple0("TXS", "Transfer X to S") { transfer(rX, rS) }
        val txy = OperationSimple0("TXY", "Transfer X to Y") { transfer(rX, rY) }
        val tya = OperationSimple0("TYA", "Transfer Y to A") { transfer(rY, rA) }
        val tyx = OperationSimple0("TYX", "Transfer Y to X") { transfer(rY, rX) }
        val wai = OperationSimple0("WAI", "Wait for interrupt") { waitForInterrupt() }
        val wdm = OperationSimple0("WDM", "Reserved for future use") { }
        val xba = OperationSimple0("XBA", "Exchange B and A") { xba() }
        val xce = OperationSimple0("XCE", "Exchange Carry and Emulation flag") { xce() }

        /**
         * An Operation the reads input from accumulator or memory and executes with that
         */
        open inner class OperationRead(
            symbol: String,
            description: String,
            val r: Register = rA,
            val action: (Int) -> Any?
        ) : OperationBase(
            symbol, description
        ) {
            override fun execute() {
                with(instData) {
                    readValue(r)

                    action(value)
                }
            }
        }

        /**
         * An Operation that reads an input from accumulator or memory and rewrites the corresponding value with the result
         */
        inner class OperationSet(
            symbol: String,
            description: String,
            val r: Register = rA,
            val action: (Int) -> Int
        ) : OperationBase(
            symbol,
            description
        ) {
            override fun execute() {
                with(instData) {
                    readValue(r)

                    val res = action(value)

                    writeValue(res, r)
                }
            }
        }

        /**
         * An Operation that operates on a given bank-address
         */
        inner class OperationAddress(
            symbol: String,
            description: String,
            val action: (Bank, ShortAddress) -> Any?
        ) : OperationBase(
            symbol,
            description
        ) {
            override fun execute() {
                with(instData) {
                    action(bank, address)
                }
            }
        }
    }

    internal inner class Instructions {
        private val instructions = arrayOf(
            /* 0x00 */ Instruction(operations.brk, addressModes.stack),
            /* 0x01 */ Instruction(operations.ora, addressModes.directIndexedIndirect),
            /* 0x02 */ Instruction(operations.cop, addressModes.stack),
            /* 0x03 */ Instruction(operations.ora, addressModes.stackRelative),
            /* 0x04 */ Instruction(operations.tsb, addressModes.direct),
            /* 0x05 */ Instruction(operations.ora, addressModes.direct),
            /* 0x06 */ Instruction(operations.asl, addressModes.direct),
            /* 0x07 */ Instruction(operations.ora, addressModes.directIndirectLong),
            /* 0x08 */ Instruction(operations.php, addressModes.stack),
            /* 0x09 */ Instruction(operations.ora, addressModes.immediate),
            /* 0x0A */ Instruction(operations.asl, addressModes.accumulator),
            /* 0x0B */ Instruction(operations.phd, addressModes.stack),
            /* 0x0C */ Instruction(operations.tsb, addressModes.absolute),
            /* 0x0D */ Instruction(operations.ora, addressModes.absolute),
            /* 0x0E */ Instruction(operations.asl, addressModes.absolute),
            /* 0x0F */ Instruction(operations.ora, addressModes.absoluteLong),
            /* 0x10 */ Instruction(operations.bpl, addressModes.programCounterRelative),
            /* 0x11 */ Instruction(operations.ora, addressModes.directIndirectIndexed),
            /* 0x12 */ Instruction(operations.ora, addressModes.directIndirect),
            /* 0x13 */ Instruction(operations.ora, addressModes.stackRelativeIndirectIndexed),
            /* 0x14 */ Instruction(operations.trb, addressModes.direct),
            /* 0x15 */ Instruction(operations.ora, addressModes.directIndexedWithX),
            /* 0x16 */ Instruction(operations.asl, addressModes.directIndexedWithX),
            /* 0x17 */ Instruction(operations.ora, addressModes.directIndirectLongIndexed),
            /* 0x18 */ Instruction(operations.clc, addressModes.implied),
            /* 0x19 */ Instruction(operations.ora, addressModes.absoluteIndexedWithY),
            /* 0x1A */ Instruction(operations.inc, addressModes.accumulator),
            /* 0x1B */ Instruction(operations.tcs, addressModes.implied),
            /* 0x1C */ Instruction(operations.trb, addressModes.absolute),
            /* 0x1D */ Instruction(operations.ora, addressModes.absoluteIndexedWithX),
            /* 0x1E */ Instruction(operations.asl, addressModes.absoluteIndexedWithX),
            /* 0x1F */ Instruction(operations.ora, addressModes.absoluteLongIndexedWithX),
            /* 0x20 */ Instruction(operations.jsr, addressModes.absolute),
            /* 0x21 */ Instruction(operations.and, addressModes.directIndexedIndirect),
            /* 0x22 */ Instruction(operations.jsl, addressModes.absoluteLong),
            /* 0x23 */ Instruction(operations.and, addressModes.stackRelative),
            /* 0x24 */ Instruction(operations.bit, addressModes.direct),
            /* 0x25 */ Instruction(operations.and, addressModes.direct),
            /* 0x26 */ Instruction(operations.rol, addressModes.direct),
            /* 0x27 */ Instruction(operations.and, addressModes.directIndirectLong),
            /* 0x28 */ Instruction(operations.plp, addressModes.stack),
            /* 0x29 */ Instruction(operations.and, addressModes.immediate),
            /* 0x2A */ Instruction(operations.rol, addressModes.accumulator),
            /* 0x2B */ Instruction(operations.pld, addressModes.stack),
            /* 0x2C */ Instruction(operations.bit, addressModes.absolute),
            /* 0x2D */ Instruction(operations.and, addressModes.absolute),
            /* 0x2E */ Instruction(operations.rol, addressModes.absolute),
            /* 0x2F */ Instruction(operations.and, addressModes.absoluteLong),
            /* 0x30 */ Instruction(operations.bmi, addressModes.programCounterRelative),
            /* 0x31 */ Instruction(operations.and, addressModes.directIndirectIndexed),
            /* 0x32 */ Instruction(operations.and, addressModes.directIndirect),
            /* 0x33 */ Instruction(operations.and, addressModes.stackRelativeIndirectIndexed),
            /* 0x34 */ Instruction(operations.bit, addressModes.directIndexedWithX),
            /* 0x35 */ Instruction(operations.and, addressModes.directIndexedWithX),
            /* 0x36 */ Instruction(operations.rol, addressModes.directIndexedWithX),
            /* 0x37 */ Instruction(operations.and, addressModes.directIndirectLongIndexed),
            /* 0x38 */ Instruction(operations.sec, addressModes.implied),
            /* 0x39 */ Instruction(operations.and, addressModes.absoluteIndexedWithY),
            /* 0x3A */ Instruction(operations.dec, addressModes.accumulator),
            /* 0x3B */ Instruction(operations.tsc, addressModes.implied),
            /* 0x3C */ Instruction(operations.bit, addressModes.absoluteIndexedWithX),
            /* 0x3D */ Instruction(operations.and, addressModes.absoluteIndexedWithX),
            /* 0x3E */ Instruction(operations.rol, addressModes.absoluteIndexedWithX),
            /* 0x3F */ Instruction(operations.and, addressModes.absoluteLongIndexedWithX),
            /* 0x40 */ Instruction(operations.rti, addressModes.stack),
            /* 0x41 */ Instruction(operations.eor, addressModes.directIndexedIndirect),
            /* 0x42 */ Instruction(operations.wdm, addressModes.addressNull),
            /* 0x43 */ Instruction(operations.eor, addressModes.stackRelative),
            /* 0x44 */ Instruction(operations.mvp, addressModes.blockMove),
            /* 0x45 */ Instruction(operations.eor, addressModes.direct),
            /* 0x46 */ Instruction(operations.lsr, addressModes.direct),
            /* 0x47 */ Instruction(operations.eor, addressModes.directIndirectLong),
            /* 0x48 */ Instruction(operations.pha, addressModes.stack),
            /* 0x49 */ Instruction(operations.eor, addressModes.immediate),
            /* 0x4A */ Instruction(operations.lsr, addressModes.accumulator),
            /* 0x4B */ Instruction(operations.phk, addressModes.stack),
            /* 0x4C */ Instruction(operations.jmp, addressModes.absolute),
            /* 0x4D */ Instruction(operations.eor, addressModes.absolute),
            /* 0x4E */ Instruction(operations.lsr, addressModes.absolute),
            /* 0x4F */ Instruction(operations.eor, addressModes.absoluteLong),
            /* 0x50 */ Instruction(operations.bvc, addressModes.programCounterRelative),
            /* 0x51 */ Instruction(operations.eor, addressModes.directIndirectIndexed),
            /* 0x52 */ Instruction(operations.eor, addressModes.directIndirect),
            /* 0x53 */ Instruction(operations.eor, addressModes.stackRelativeIndirectIndexed),
            /* 0x54 */ Instruction(operations.mvn, addressModes.blockMove),
            /* 0x55 */ Instruction(operations.eor, addressModes.directIndexedWithX),
            /* 0x56 */ Instruction(operations.lsr, addressModes.directIndexedWithX),
            /* 0x57 */ Instruction(operations.eor, addressModes.directIndirectLongIndexed),
            /* 0x58 */ Instruction(operations.cli, addressModes.implied),
            /* 0x59 */ Instruction(operations.eor, addressModes.absoluteIndexedWithY),
            /* 0x5A */ Instruction(operations.phy, addressModes.stack),
            /* 0x5B */ Instruction(operations.tcd, addressModes.implied),
            /* 0x5C */ Instruction(operations.jml, addressModes.absoluteLong),
            /* 0x5D */ Instruction(operations.eor, addressModes.absoluteIndexedWithX),
            /* 0x5E */ Instruction(operations.lsr, addressModes.absoluteIndexedWithX),
            /* 0x5F */ Instruction(operations.eor, addressModes.absoluteLongIndexedWithX),
            /* 0x60 */ Instruction(operations.rts, addressModes.stack),
            /* 0x61 */ Instruction(operations.adc, addressModes.directIndexedIndirect),
            /* 0x62 */ Instruction(operations.per, addressModes.programCounterRelativeLong),
            /* 0x63 */ Instruction(operations.adc, addressModes.stackRelative),
            /* 0x64 */ Instruction(operations.stz, addressModes.direct),
            /* 0x65 */ Instruction(operations.adc, addressModes.direct),
            /* 0x66 */ Instruction(operations.ror, addressModes.direct),
            /* 0x67 */ Instruction(operations.adc, addressModes.directIndirectLong),
            /* 0x68 */ Instruction(operations.pla, addressModes.stack),
            /* 0x69 */ Instruction(operations.adc, addressModes.immediate),
            /* 0x6A */ Instruction(operations.ror, addressModes.accumulator),
            /* 0x6B */ Instruction(operations.rtl, addressModes.stack),
            /* 0x6C */ Instruction(operations.jmp, addressModes.absoluteIndirect),
            /* 0x6D */ Instruction(operations.adc, addressModes.absolute),
            /* 0x6E */ Instruction(operations.ror, addressModes.absolute),
            /* 0x6F */ Instruction(operations.adc, addressModes.absoluteLong),
            /* 0x70 */ Instruction(operations.bvs, addressModes.programCounterRelative),
            /* 0x71 */ Instruction(operations.adc, addressModes.directIndirectIndexed),
            /* 0x72 */ Instruction(operations.adc, addressModes.directIndirect),
            /* 0x73 */ Instruction(operations.adc, addressModes.stackRelativeIndirectIndexed),
            /* 0x74 */ Instruction(operations.stz, addressModes.directIndexedWithX),
            /* 0x75 */ Instruction(operations.adc, addressModes.directIndexedWithX),
            /* 0x76 */ Instruction(operations.ror, addressModes.directIndexedWithX),
            /* 0x77 */ Instruction(operations.adc, addressModes.directIndirectLongIndexed),
            /* 0x78 */ Instruction(operations.sei, addressModes.implied),
            /* 0x79 */ Instruction(operations.adc, addressModes.absoluteIndexedWithY),
            /* 0x7A */ Instruction(operations.ply, addressModes.stack),
            /* 0x7B */ Instruction(operations.tdc, addressModes.implied),
            /* 0x7C */ Instruction(operations.jmp, addressModes.absoluteIndexedIndirect),
            /* 0x7D */ Instruction(operations.adc, addressModes.absoluteIndexedWithX),
            /* 0x7E */ Instruction(operations.ror, addressModes.absoluteIndexedWithX),
            /* 0x7F */ Instruction(operations.adc, addressModes.absoluteLongIndexedWithX),
            /* 0x80 */ Instruction(operations.bra, addressModes.programCounterRelative),
            /* 0x81 */ Instruction(operations.sta, addressModes.directIndexedIndirect),
            /* 0x82 */ Instruction(operations.brl, addressModes.programCounterRelativeLong),
            /* 0x83 */ Instruction(operations.sta, addressModes.stackRelative),
            /* 0x84 */ Instruction(operations.sty, addressModes.direct),
            /* 0x85 */ Instruction(operations.sta, addressModes.direct),
            /* 0x86 */ Instruction(operations.stx, addressModes.direct),
            /* 0x87 */ Instruction(operations.sta, addressModes.directIndirectLong),
            /* 0x88 */ Instruction(operations.dey, addressModes.implied),
            /* 0x89 */ Instruction(operations.bit, addressModes.immediate),
            /* 0x8A */ Instruction(operations.txa, addressModes.implied),
            /* 0x8B */ Instruction(operations.phb, addressModes.stack),
            /* 0x8C */ Instruction(operations.sty, addressModes.absolute),
            /* 0x8D */ Instruction(operations.sta, addressModes.absolute),
            /* 0x8E */ Instruction(operations.stx, addressModes.absolute),
            /* 0x8F */ Instruction(operations.sta, addressModes.absoluteLong),
            /* 0x90 */ Instruction(operations.bcc, addressModes.programCounterRelative),
            /* 0x91 */ Instruction(operations.sta, addressModes.directIndirectIndexed),
            /* 0x92 */ Instruction(operations.sta, addressModes.directIndirect),
            /* 0x93 */ Instruction(operations.sta, addressModes.stackRelativeIndirectIndexed),
            /* 0x94 */ Instruction(operations.sty, addressModes.directIndexedWithX),
            /* 0x95 */ Instruction(operations.sta, addressModes.directIndexedWithX),
            /* 0x96 */ Instruction(operations.stx, addressModes.directIndexedWithY),
            /* 0x97 */ Instruction(operations.sta, addressModes.directIndirectLongIndexed),
            /* 0x98 */ Instruction(operations.tya, addressModes.implied),
            /* 0x99 */ Instruction(operations.sta, addressModes.absoluteIndexedWithY),
            /* 0x9A */ Instruction(operations.txs, addressModes.implied),
            /* 0x9B */ Instruction(operations.txy, addressModes.implied),
            /* 0x9C */ Instruction(operations.stz, addressModes.absolute),
            /* 0x9D */ Instruction(operations.sta, addressModes.absoluteIndexedWithX),
            /* 0x9E */ Instruction(operations.stz, addressModes.absoluteIndexedWithX),
            /* 0x9F */ Instruction(operations.sta, addressModes.absoluteLongIndexedWithX),
            /* 0xA0 */ Instruction(operations.ldy, addressModes.immediate),
            /* 0xA1 */ Instruction(operations.lda, addressModes.directIndexedIndirect),
            /* 0xA2 */ Instruction(operations.ldx, addressModes.immediate),
            /* 0xA3 */ Instruction(operations.lda, addressModes.stackRelative),
            /* 0xA4 */ Instruction(operations.ldy, addressModes.direct),
            /* 0xA5 */ Instruction(operations.lda, addressModes.direct),
            /* 0xA6 */ Instruction(operations.ldx, addressModes.direct),
            /* 0xA7 */ Instruction(operations.lda, addressModes.directIndirectLong),
            /* 0xA8 */ Instruction(operations.tay, addressModes.implied),
            /* 0xA9 */ Instruction(operations.lda, addressModes.immediate),
            /* 0xAA */ Instruction(operations.tax, addressModes.implied),
            /* 0xAB */ Instruction(operations.plb, addressModes.stack),
            /* 0xAC */ Instruction(operations.ldy, addressModes.absolute),
            /* 0xAD */ Instruction(operations.lda, addressModes.absolute),
            /* 0xAE */ Instruction(operations.ldx, addressModes.absolute),
            /* 0xAF */ Instruction(operations.lda, addressModes.absoluteLong),
            /* 0xB0 */ Instruction(operations.bcs, addressModes.programCounterRelative),
            /* 0xB1 */ Instruction(operations.lda, addressModes.directIndirectIndexed),
            /* 0xB2 */ Instruction(operations.lda, addressModes.directIndirect),
            /* 0xB3 */ Instruction(operations.lda, addressModes.stackRelativeIndirectIndexed),
            /* 0xB4 */ Instruction(operations.ldy, addressModes.directIndexedWithX),
            /* 0xB5 */ Instruction(operations.lda, addressModes.directIndexedWithX),
            /* 0xB6 */ Instruction(operations.ldx, addressModes.directIndexedWithY),
            /* 0xB7 */ Instruction(operations.lda, addressModes.directIndirectLongIndexed),
            /* 0xB8 */ Instruction(operations.clv, addressModes.implied),
            /* 0xB9 */ Instruction(operations.lda, addressModes.absoluteIndexedWithY),
            /* 0xBA */ Instruction(operations.tsx, addressModes.implied),
            /* 0xBB */ Instruction(operations.tyx, addressModes.implied),
            /* 0xBC */ Instruction(operations.ldy, addressModes.absoluteIndexedWithX),
            /* 0xBD */ Instruction(operations.lda, addressModes.absoluteIndexedWithX),
            /* 0xBE */ Instruction(operations.ldx, addressModes.absoluteIndexedWithY),
            /* 0xBF */ Instruction(operations.lda, addressModes.absoluteLongIndexedWithX),
            /* 0xC0 */ Instruction(operations.cpy, addressModes.immediate),
            /* 0xC1 */ Instruction(operations.cmp, addressModes.directIndexedIndirect),
            /* 0xC2 */ Instruction(operations.rep, addressModes.immediate),
            /* 0xC3 */ Instruction(operations.cmp, addressModes.stackRelative),
            /* 0xC4 */ Instruction(operations.cpy, addressModes.direct),
            /* 0xC5 */ Instruction(operations.cmp, addressModes.direct),
            /* 0xC6 */ Instruction(operations.dec, addressModes.direct),
            /* 0xC7 */ Instruction(operations.cmp, addressModes.directIndirectLong),
            /* 0xC8 */ Instruction(operations.iny, addressModes.implied),
            /* 0xC9 */ Instruction(operations.cmp, addressModes.immediate),
            /* 0xCA */ Instruction(operations.dex, addressModes.implied),
            /* 0xCB */ Instruction(operations.wai, addressModes.implied),
            /* 0xCC */ Instruction(operations.cpy, addressModes.absolute),
            /* 0xCD */ Instruction(operations.cmp, addressModes.absolute),
            /* 0xCE */ Instruction(operations.dec, addressModes.absolute),
            /* 0xCF */ Instruction(operations.cmp, addressModes.absoluteLong),
            /* 0xD0 */ Instruction(operations.bne, addressModes.programCounterRelative),
            /* 0xD1 */ Instruction(operations.cmp, addressModes.directIndirectIndexed),
            /* 0xD2 */ Instruction(operations.cmp, addressModes.directIndirect),
            /* 0xD3 */ Instruction(operations.cmp, addressModes.stackRelativeIndirectIndexed),
            /* 0xD4 */ Instruction(operations.pei, addressModes.direct),
            /* 0xD5 */ Instruction(operations.cmp, addressModes.directIndexedWithX),
            /* 0xD6 */ Instruction(operations.dec, addressModes.directIndexedWithX),
            /* 0xD7 */ Instruction(operations.cmp, addressModes.directIndirectLongIndexed),
            /* 0xD8 */ Instruction(operations.cld, addressModes.implied),
            /* 0xD9 */ Instruction(operations.cmp, addressModes.absoluteIndexedWithY),
            /* 0xDA */ Instruction(operations.phx, addressModes.stack),
            /* 0xDB */ Instruction(operations.stp, addressModes.implied),
            /* 0xDC */ Instruction(operations.jml, addressModes.absoluteIndirectLong),
            /* 0xDD */ Instruction(operations.cmp, addressModes.absoluteIndexedWithX),
            /* 0xDE */ Instruction(operations.dec, addressModes.absoluteIndexedWithX),
            /* 0xDF */ Instruction(operations.cmp, addressModes.absoluteLongIndexedWithX),
            /* 0xE0 */ Instruction(operations.cpx, addressModes.immediate),
            /* 0xE1 */ Instruction(operations.sbc, addressModes.directIndexedIndirect),
            /* 0xE2 */ Instruction(operations.sep, addressModes.immediate),
            /* 0xE3 */ Instruction(operations.sbc, addressModes.stackRelative),
            /* 0xE4 */ Instruction(operations.cpx, addressModes.direct),
            /* 0xE5 */ Instruction(operations.sbc, addressModes.direct),
            /* 0xE6 */ Instruction(operations.inc, addressModes.direct),
            /* 0xE7 */ Instruction(operations.sbc, addressModes.directIndirectLong),
            /* 0xE8 */ Instruction(operations.inx, addressModes.implied),
            /* 0xE9 */ Instruction(operations.sbc, addressModes.immediate),
            /* 0xEA */ Instruction(operations.nop, addressModes.implied),
            /* 0xEB */ Instruction(operations.xba, addressModes.implied),
            /* 0xEC */ Instruction(operations.cpx, addressModes.absolute),
            /* 0xED */ Instruction(operations.sbc, addressModes.absolute),
            /* 0xEE */ Instruction(operations.inc, addressModes.absolute),
            /* 0xEF */ Instruction(operations.sbc, addressModes.absoluteLong),
            /* 0xF0 */ Instruction(operations.beq, addressModes.programCounterRelative),
            /* 0xF1 */ Instruction(operations.sbc, addressModes.directIndirectIndexed),
            /* 0xF2 */ Instruction(operations.sbc, addressModes.directIndirect),
            /* 0xF3 */ Instruction(operations.sbc, addressModes.stackRelativeIndirectIndexed),
            /* 0xF4 */ Instruction(operations.pea, addressModes.absolute),
            /* 0xF5 */ Instruction(operations.sbc, addressModes.directIndexedWithX),
            /* 0xF6 */ Instruction(operations.inc, addressModes.directIndexedWithX),
            /* 0xF7 */ Instruction(operations.sbc, addressModes.directIndirectLongIndexed),
            /* 0xF8 */ Instruction(operations.sed, addressModes.implied),
            /* 0xF9 */ Instruction(operations.sbc, addressModes.absoluteIndexedWithY),
            /* 0xFA */ Instruction(operations.plx, addressModes.stack),
            /* 0xFB */ Instruction(operations.xce, addressModes.implied),
            /* 0xFC */ Instruction(operations.jsr, addressModes.absoluteIndexedIndirect),
            /* 0xFD */ Instruction(operations.sbc, addressModes.absoluteIndexedWithX),
            /* 0xFE */ Instruction(operations.inc, addressModes.absoluteIndexedWithX),
            /* 0xFF */ Instruction(operations.sbc, addressModes.absoluteLongIndexedWithX)
        )

        operator fun get(index: Int) = instructions[index]
    }

    private inner class InstructionData {
        lateinit var inst: Instruction
        var address: ShortAddress = -1
        var bank: Int = -1
        var result: AddressModeResult = AddressModeResult.NOTHING
        var value: Int = -1

        fun updateInst(inst: Instruction) {
            this.inst = inst
            result = inst.addressMode.result
            when {
                result.address -> {
                    value = inst.addressMode.fetchValue()

                    bank = when (result) {
                        AddressModeResult.ADDRESS_0 -> 0
                        AddressModeResult.FULLADDRESS -> value.bank
                        AddressModeResult.SHORTADDRESS,
                        AddressModeResult.ADDRESS_PBR -> rPBR.get()
                        AddressModeResult.ADDRESS_DBR -> rDBR.get()
                        else -> error("should never be possible to happen")
                    }
                    address = when (result) {
                        AddressModeResult.ADDRESS_PBR,
                        AddressModeResult.ADDRESS_0,
                        AddressModeResult.ADDRESS_DBR,
                        AddressModeResult.SHORTADDRESS -> value
                        AddressModeResult.FULLADDRESS -> value.shortAddress
                        else -> error("should never be possible to happen")
                    }

                    value = -1
                }
                result == AddressModeResult.IMMEDIATE ||
                        result.value -> {
                    address = -1
                    value = inst.addressMode.fetchValue()
                }
                else -> {
                    address = -1
                    value = -1
                }
            }
        }

        fun readValue(r: Register) {
            if (address != -1) {
                value = read(bank, address, r.size)
            } else if (value != -1 && result == AddressModeResult.IMMEDIATE && r.size > 1) {
                value = Short(value, inst.addressMode.fetchValue())
            }
        }

        fun writeValue(res: Int, r: Register) {
            if (address != -1) {
                write(bank, address, res, r.size)
            } else if (result == AddressModeResult.ACCUMULATOR) {
                rA.set(res)
            }
        }
    }

    fun getInstruction(opCode: Int)
        = instructions[opCode]

    companion object {
        const val EMULATION_COP_VECTOR_ADDRESS: ShortAddress = 0xFFF4
        const val EMULATION_ABORT_VECTOR_ADDRESS: ShortAddress = 0xFFF8
        const val EMULATION_NMI_VECTOR_ADDRESS: ShortAddress = 0xFFFA
        const val EMULATION_RESET_VECTOR_ADDRESS: ShortAddress = 0xFFFC
        const val EMULATION_IRQ_VECTOR_ADDRESS: ShortAddress = 0xFFFE
        const val EMULATION_BRK_VECTOR_ADDRESS: ShortAddress = EMULATION_IRQ_VECTOR_ADDRESS

        const val NATIVE_COP_VECTOR_ADDRESS: ShortAddress = 0xFFE4
        const val NATIVE_BRK_VECTOR_ADDRESS: ShortAddress = 0xFFE6
        const val NATIVE_ABORT_VECTOR_ADDRESS: ShortAddress = 0xFFE8
        const val NATIVE_NMI_VECTOR_ADDRESS: ShortAddress = 0xFFEA
        //const val NATIVE_RESET_VECTOR_ADDRESS: ShortAddress = 0xFFFC // not needed, as reset is always in emulation mode
        const val NATIVE_IRQ_VECTOR_ADDRESS: ShortAddress = 0xFFEE

        // cycles for memory access
        private const val ONE_CYCLE = 6
        private const val TWO_CYCLES = 2 * ONE_CYCLE
        private const val SLOW_ONE_CYCLE = 8

        // cycles per transferred byte in dma = 8

        // cpu is halted during dma/hdma
        // hdma has prio over dma
        // hdma channels are deactivated at v-blank
        // hdma starts at h 278
        // auto joypad read at 32.5 and 95.5 in first scanline of v-blank
        // h-blank from 274 to 1
        // v-blank starts at e1 (2133.2 cleared) or f0
        // at beginning of v-blank, oam-internal-address is reset to 2102-2103
    }
}
