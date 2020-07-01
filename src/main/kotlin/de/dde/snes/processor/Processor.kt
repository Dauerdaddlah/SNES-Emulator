package de.dde.snes.processor

import de.dde.snes.memory.*


class Processor(
    val memory: Memory
) {
    // TODO consider cycles for everything else than memory-access
    var mode = ProcessorMode.EMULATION

    // registers
    /** accumulator */
    var rA = Accumulator()
    /** Data Bank Register */
    var rDBR = Register8Bit()
    /** Direct */
    var rD = Register16Bit()
    /** X Index Register */
    var rX = IndexRegister()
    /** Y Index Register */
    var rY = IndexRegister()
    /** Processor Status Register */
    var rP = StatusRegister()
    /** Program Bank Register */
    var rPBR = Register8Bit()
    /** Program Counter */
    var rPC = Register16Bit()
    /** Stack Pointer */
    var rS = StackPointer()

    private val operands = Operands()
    private val instructions = Instructions()

    var waitForInterrupt = false
        private set

    var cycles = 0
        private set

    fun reset() {
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

        rPC.value = readWord(
            rDBR.value,
            RESET_VECTOR_ADDRESS
        )
    }

    fun executeNextInstruction() {
        if (waitForInterrupt) {
            return
        }
        val i = fetch()
        val inst = instructions[i]

        //println(inst)

        inst.execute()
    }

    private fun fetch(): Int {
        val v = readByte(
            rPBR.value,
            rPC.value
        )
        rPC.inc()
        return v
    }

    private fun fetchShort()
        = fetch() or (fetch() shl 8)
    private fun fetchLong()
        = fetchShort() or (fetch() shl 16)

    fun NMI() {
        interrupt(NMI_VECTOR_ADDRESS)
    }

    /** break interrupt */
    private fun instBRK() {
        interrupt(if (mode == ProcessorMode.EMULATION) EMULATION_BRK_VECTOR_ADDRESS else NATIVE_BRK_VECTOR_ADDRESS)
    }

    /** coprocessor interrupt */
    private fun instCOP() {
        interrupt(COP_VECTOR_ADDRESS)
    }

    private fun interrupt(interruptAddress: ShortAddress) {
        waitForInterrupt = false

        if (mode == ProcessorMode.NATIVE) {
            rS.pushByte(rPBR.value)
        }

        rS.pushShort(rPC.value + 1)
        rS.pushByte(rP.asInt())

        rP.decimal = false
        rP.irqDisable = true

        if (mode == ProcessorMode.NATIVE) {
            rPBR.value = 0
        }

        rPC.value = readWord(0, interruptAddress)
    }

    /** Test and Set Bit */
    private fun instTSB(value: Int): Int {
        rP.zero = rA and value == 0
        return rA.valueOf(rA or value)
    }

    /** Test and Reset Bit */
    private fun instTRB(value: Int): Int {
        rP.zero = rA and value == 0
        return rA.valueOf(value and rA.inv)
    }

    /** Bit-test between A and the given value - no value is changed, only flags are set */
    private fun instBIT(value: Int) {
        val res = rA and value
        rP.zero = res == 0
        rP.overflow = rA.overflow(value)
        rP.negative = rA.negative(value)
    }

    /** Bit-test between A and the given value - no value is changed, only flags are set  - in this one special isntance, only the zero flag is set */
    private fun instBITImmediate(value: Int) {
        val res = rA and value
        rP.zero = res == 0
    }

    /** Set carry */
    private fun instSEC() {
        rP.carry = true
    }

    /** Set irq/interrupt flag */
    private fun instSEI() {
        rP.irqDisable = true
    }

    /** Set decimal flag */
    private fun instSED() {
        rP.decimal = true
    }

    /** Clear carry flag */
    private fun instCLC() {
        rP.carry = false
    }

    /** clear irq/interrupt flag */
    private fun instCLI() {
        rP.irqDisable = false
    }

    /** Clear decimal flag */
    private fun instCLD() {
        rP.decimal = false
    }

    /** Clear overflow flag */
    private fun instCLV() {
        rP.overflow = false
    }

    /** Set Bit in P */
    private fun instSEP(value: Int) {
        rP.fromInt(rP.asInt() or value)
        rA.checkBitMode()
        rX.checkBitMode()
        rY.checkBitMode()
    }

    /** Reset Bit in P */
    private fun instREP(value: Int) {
        // reset any bit of P set in value
        var p = rP.asInt()
        p = p and value.inv()
        rP.fromInt(p)
        rA.checkBitMode()
        rX.checkBitMode()
        rY.checkBitMode()
    }

    /** Exchange Carry and Emulation-flag */
    private fun instXCE() {
        val t = rP.carry
        rP.carry = mode == ProcessorMode.EMULATION
        mode = if (t) ProcessorMode.EMULATION else ProcessorMode.NATIVE
        rP.fromInt(rP.asInt())

        rA.checkBitMode()
        rX.checkBitMode()
        rY.checkBitMode()
    }

    /** Compare given value with A */
    private fun instCMP(value: Int) {
        compare(value, rA)
    }

    /** Compare the given value with X */
    private fun instCPX(value: Int) {
        compare(value, rX)
    }

    /** Compare given value with Y */
    private fun instCPY(value: Int) {
        compare(value, rY)
    }

    private fun compare(value: Int, register: DiffSizeRegister) {
        val v = register.get() - register.valueOf(value)
        rP.carry = v >= 0
        rP.zero = v == 0
        rP.negative = register.negative(v)
    }

    /** branch if plus - branches if negative is not set */
    private fun instBPL(address: FullAddress) {
        branchIf(address) {!rP.negative }
    }

    /** Branch if minus - branches if negative is set */
    private fun instBMI(address: FullAddress) {
        branchIf(address) { rP.negative }
    }

    /** Branch if overflow set */
    private fun instBVS(address: FullAddress) {

        branchIf(address) { rP.overflow }
    }

    /** branch if overflow clear */
    private fun instBVC(address: FullAddress) {
        branchIf(address) { !rP.overflow }
    }

    /** Branch if carry set */
    private fun instBCS(address: FullAddress) {
        branchIf(address) { rP.carry }
    }

    /** Branch if carry clear */
    private fun instBCC(address: FullAddress) {
        branchIf(address) {!rP.carry }
    }

    /** Branch if zero set/equal */
    private fun instBEQ(address: FullAddress) {
        branchIf(address) { rP.zero }
    }

    /** Branch if Zero clear/not Equal */
    private fun instBNE(address: FullAddress) {
        branchIf(address) { !rP.zero }
    }

    private fun branchIf(address: FullAddress, check: () -> Boolean) {
        if (check()) {
            branch(address)
        }
    }

    /** Branch always */
    private fun instBRA(address: FullAddress) {
        branch(address)
    }

    /** Branch always long */
    private fun instBRL(address: FullAddress) {
        branch(address)
    }

    /** Jump to subroutine (given address without bank) - pushes the current PC minus 1 */
    private fun instJSR(address: FullAddress) {
        jump(address)
    }

    /** Jump to subroutine (given address including bank), pushes the current PC minus 1 and the PBR */
    private fun instJSL(address: FullAddress) {
        jumpLong(address)
    }

    /** Jump to full address including bank */
    private fun instJMP(address: FullAddress) {
        branch(address)
    }

    /** Jump Long - jump to the given address bank included */
    private fun instJML(address: FullAddress) {
        branchLong(address)
    }

    private fun branch(address: FullAddress) {
        rPC.value = address.shortAddress
    }

    private fun branchLong(address: FullAddress) {
        rPC.value = address.shortAddress
        rPBR.value = address.bank
    }

    private fun jump(address: ShortAddress) {
        rS.pushShort(rPC.value - 1)
        branch(address)
    }

    private fun jumpLong(address: FullAddress) {
        rS.pushByte(rPBR.value)
        rS.pushShort(rPC.value - 1)
        branchLong(address)
    }

    /** return from interrupt */
    private fun instRTI() {
        rP.fromInt(rS.pullByte())

        rA.checkBitMode()
        rX.checkBitMode()
        rY.checkBitMode()

        rPC.value = rS.pullShort()

        if (mode == ProcessorMode.NATIVE) {
            rPBR.value = rS.pullByte()
        }
    }

    /** return from subroutine */
    private fun instRTS() {
        rPC.value = rS.pullShort()
        rPC.inc()
    }

    /** Return from Subroutine long */
    private fun instRTL() {
        rPC.value = rS.pullShort()
        rPBR.value = rS.pullByte()
        rPC.inc()
    }

    /** perform or with accumulator */
    private fun instORA(value: Int) {
        rA.set(rA or value)
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** perform and with accumulator */
    private fun instAND(value: Int) {
        rA.set(rA and value)
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** perform exlusive or with A */
    private fun instEOR(value: Int) {
        rA.set(rA xor value)
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** shift left */
    private fun instASL(value: Int): Int {
        rP.carry = rA.negative(value)
        val v = rA.valueOf(value shl 1)
        rP.zero = v == 0
        rP.negative = rA.negative(v)
        return v
    }

    /** shift right without carry */
    private fun instLSR(value: Int): Int {
        rP.carry = value and 1 != 0
        val v = value shr 1
        rP.zero = v == 0
        rP.negative = rA.negative(v)
        return v
    }

    /** shift left with carry */
    private fun instROL(value: Int): Int {
        var v = value shl 1
        if (rP.carry) v++

        val vv = rA.valueOf(v)

        // v is shifted and has one bit more than vv if and overflow occured
        rP.carry = v != vv
        rP.zero = vv != 0
        rP.negative = rA.negative(vv)
        return vv
    }

    /** shift right with carry */
    private fun instROR(register: DiffSizeRegister, value: Int): Int {
        var res = value
        if (rP.carry) {
            res += if (register._8bitMode) 0x100 else 0x10000
        }
        res = res shr 1
        // if carry is set, the left-most bit is added after rotating to right, this is effectively the negative-bit
        rP.negative = rP.carry
        rP.carry = value and 0x1 != 0
        rP.zero = res == 0
        return res
    }

    /** Increment */
    private fun instINC(value: Int): Int {
        val v = rA.valueOf(value + 1)
        rP.zero = rA.zero(v)
        rP.negative = rA.negative(v)
        return v
    }

    /** Increment X */
    private fun instINX() {
        rX.inc()
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** Increment Y */
    private fun instINY() {
        rY.inc()
        rP.zero = rY.zero
        rP.negative = rY.negative
    }

    /** Decrement */
    private fun instDEC(value: Int): Int {
        val v = rA.valueOf(value - 1)
        rP.zero = rA.zero(v)
        rP.negative = rA.negative(v)
        return v
    }

    /** Decrement X */
    private fun instDEX() {
        rX.dec()
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** Decrement Y */
    private fun instDEY() {
        rY.dec()
        rP.zero = rY.zero
        rP.negative = rY.negative
    }

    /** add with carry */
    private fun instADC(value: Int) {
        // TODO make pretty, currently its just copied from snes9x
        var v = if (rP.decimal) {
            var res = (rA.value and 0xF) + (value and 0xF) + (if (rP.carry) 0x1 else 0)
            if (res > 0x9) res += 0x6
            rP.carry = res > 0xF
            res = (rA.value and 0xF0) + (value and 0xF0) + (if (rP.carry) 0x10 else 0) + (res and 0xF)
            if (!rA._8bitMode) {
                if (res > 0x9F) res += 0x60
                rP.carry = res > 0xFF
                res = (rA.value and 0xF00) + (value and 0xF00) + (if (rP.carry) 0x100 else 0) + (res and 0xFF)
                if (res > 0x9FF) res += 0x600
                rP.carry = res > 0xFFF
                res = (rA.value and 0xF000) + (value and 0xF000) + (if (rP.carry) 0x1000 else 0) + (res and 0xFFF)
            }
            res
        } else {
            rA.get() + value + if (rP.carry) 1 else 0
        }

        // (A.value xor value).inv() the sign-bit is 1 only if both A and value have the same sign
        // (A.value xor v) the sign-bit is set, if A (the start) and v (the end) have different signs
        // so if these two are combined using and, a set sign-bit shows, that A overflowed
        rP.overflow = rA.negative((rA.value xor value).inv() and (rA.value xor v))

        if (rP.decimal) {
            if (rA._8bitMode && v > 0x9F) v += 0x60
            else if (!rA._8bitMode && v > 0x9FFF) v += 0x6000
        }

        rA.set(v)

        rP.negative = rA.negative
        rP.zero = rA.zero
        rP.carry = rA.get() != v
    }

    /** Subtract with carry */
    private fun instSBC(value: Int) {
        // TODO make pretty, currently its just copied from snes9x

        // if carry is set, the calculation is as expected (i. e. 0x9 - 0x6 = 0x3)
        // without carry, we have to subtract one more (i. e. 0x9 - 0x6 = 0x2)

        val valueInv = value.inv()
        var v = if (rP.decimal) {
            var result = (rA.value and 0xf) + (valueInv and 0xf) + (if (rP.carry) 0x1 else 0)
            if(result <= 0xf) result -= 0x6
            rP.carry = result > 0x000f
            result = (rA.value and 0xf0) + (valueInv and 0xf0) + (if (rP.carry) 0x10 else 0) + (result and 0x000f)

            if (!rA._8bitMode) {
                if (result <= 0xff) result -= 0x60
                rP.carry = result > 0xff
                result = (rA.value and  0xf00) + (valueInv and 0xf00) + (if (rP.carry) 0x100 else 0) + (result and 0x00ff)
                if (result <= 0xfff) result -= 0x600
                rP.carry = result > 0xfff
                result = (rA.value and 0xf000) + (valueInv and 0xf000) + (if (rP.carry) 0x1000 else 0) + (result and 0x0fff)
            }
            result
        } else {
            rA.get() + rA.valueOf(valueInv) + (if (rP.carry) 1 else 0)
        }

        rP.overflow = rA.negative((rA.value xor valueInv).inv() and (rA.value xor v))
        if (rP.decimal) {
            if (rA._8bitMode && v <= 0xff) v -= 0x60 else if (!rA._8bitMode && v <= 0xFFFF) v -= 0x6000
        }
        rA.set(v)
        rP.carry = v != rA.get()
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** push P */
    private fun instPHP() {
        rS.pushByte(rP.asInt())
    }

    /** push D */
    private fun instPHD() {
        rS.pushShort(rD.value)
    }

    /** push A */
    private fun instPHA() {
        if (rA._8bitMode) {
            rS.pushByte(rA.value)
        } else {
            rS.pushShort(rA.value)
        }
    }

    /** push PBR */
    private fun instPHK() {
        rS.pushByte(rPBR.value)
    }

    /** push Y */
    private fun instPHY() {
        if (rY._8bitMode) {
            rS.pushByte(rY.value)
        } else {
            rS.pushShort(rY.value)
        }
    }

    /** Push effective indirect address - pushes the given address */
    private fun instPEI(address: FullAddress) {
        rS.pushShort(address)
    }

    /** push X */
    private fun instPHX() {
        if (rX._8bitMode) {
            rS.pushByte(rX.value)
        } else {
            rS.pushShort(rX.value)
        }
    }

    /** Push effective address */
    private fun instPEA(addressInt: Int) {
        rS.pushShort(addressInt)
    }

    /** Push DBR */
    private fun instPHB() {
        rS.pushByte(rDBR.value)
    }

    /** push relative address - pushes address relative from current PC (see operand) */
    private fun instPER(address: FullAddress) {
        rS.pushShort(address)
    }

    /** Pull X */
    private fun instPLX() {
        rX.set(if (rX._8bitMode) rS.pullByte() else rS.pullShort())
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** Pull P */
    private fun instPLP() {
        rP.fromInt(rS.pullByte())

        rA.checkBitMode()
        rX.checkBitMode()
        rY.checkBitMode()
    }

    /** Pull D */
    private fun instPLD() {
        rD.value = rS.pullShort()
        rP.zero = rD.value == 0
        rP.negative = rD.negative
    }

    /** Pull A */
    private fun instPLA() {
        rA.set(if (rA._8bitMode) rS.pullByte() else rS.pullShort())
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Pull Y */
    private fun instPLY() {
        rY.set(if (rY._8bitMode) rS.pullByte() else rS.pullShort())
        rP.zero = rY.zero
        rP.negative = rY.negative
    }

    /** Pull DBR */
    private fun instPLB() {
        rDBR.value = rS.pullByte()
        rP.zero = rDBR.zero
        rP.negative = rDBR.negative
    }

    /** Load/Set value of Y */
    private fun instLDY(value: Int) {
        rY.set(value)
        rP.zero = rY.zero
        rP.negative = rY.negative
    }

    /** Load/Set value of A */
    private fun instLDA(value: Int) {
        rA.set(value)
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Load/Set value of X */
    private fun instLDX(value: Int) {
        rX.set(value)
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** Exchange B with A - swaps the 2 bytes of A */
    private fun instXBA() {
        rA.xba()
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Transfer A to S */
    private fun instTCS() {
        rS.set(rA.get())
    }

    /** Transfer S to A also setting flags */
    private fun instTSC() {
        rA.value = rS.value
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Transfer A to D */
    private fun instTCD() {
        rD.value = rA.value
        rP.zero = rD.value == 0
        rP.negative = rD.negative
    }

    /** Transfer X to A */
    private fun instTXA() {
        rA.set(rX.value)
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Transfer Y to A */
    private fun instTYA() {
        rA.set(rY.value)
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Transfer X to S */
    private fun instTXS() {
        rS.set(rX.value)
    }

    /** Transfer X to Y */
    private fun instTXY() {
        rY.set(rX.value)
        rP.zero = rY.zero
        rP.negative = rY.negative
    }

    /** Transfer A to Y */
    private fun instTAY() {
        rY.set(rA.value)
        rP.zero = rY.zero
        rP.negative = rY.negative
    }

    /** Transfer A to X */
    private fun instTAX() {
        rX.set(rA.value)
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** Transfer D to A */
    private fun instTDC() {
        rA.value = rD.value
        rP.zero = rA.zero
        rP.negative = rA.negative
    }

    /** Transfer S to X */
    private fun instTSX() {
        rX.set(rS.value)
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** Transfer Y to X */
    private fun instTYX() {
        rX.set(rY.value)
        rP.zero = rX.zero
        rP.negative = rX.negative
    }

    /** store zero */
    private fun instSTZ(address: FullAddress) {
        writeFor(rA, address, 0)
    }

    /** Store A at given address */
    private fun instSTA(address: FullAddress) {
        writeFor(rA, address)
    }

    /** Store Y at given address */
    private fun instSTY(address: FullAddress) {
        writeFor(rY, address)
    }

    /** Store X at given address */
    private fun instSTX(address: FullAddress) {
        writeFor(rX, address)
    }

    /** Block move positive */
    private fun instMVP() {
        rDBR.value = fetch()
        val bankSource = fetch()
        val bankDest = rDBR.value

        while (!rA.zero) {
            writeByte(bankDest, rY.get(),
                readByte(bankSource, rX.get()))

            rA.dec()
            rX.dec()
            rY.dec()
        }

        writeByte(bankDest, rY.get(),
            readByte(bankSource, rX.get()))

        rA.dec()
        rX.dec()
        rY.dec()
    }

    /** Block move negative */
    private fun instMVN() {
        rDBR.value = fetch()
        val bankSource = fetch()
        val bankDest = rDBR.value

        while (!rA.zero) {
            writeByte(bankDest, rY.get(),
                readByte(bankSource, rX.get()))

            rA.dec()
            rX.inc()
            rY.inc()
        }

        writeByte(bankDest, rY.get(),
            readByte(bankSource, rX.get()))

        rA.dec()
        rX.inc()
        rY.inc()
    }

    /** Wait for interrupt */
    private fun instWAI() {
        waitForInterrupt = true
    }

    /** Stop the clock */
    private fun instSTP() {
        // TODO comment: Dies.
        error("not implemented yet")
    }

    /** No Operation */
    private fun instNOP() {
    }

    /** operation reserved for future use */
    private fun instWDM() {
    }

    private fun readByte(bank: Bank, address: ShortAddress): Int {
        cycles += getCyclesForAddress(bank, address)
        return memory.readByte(bank, address)
    }

    private fun readWord(bank: Bank, address: ShortAddress)
        = readByte(bank, address) or (readByte(bank, shortAddress(address.shortAddress + 1)) shl 8)

    private fun readLong(bank: Bank, address: ShortAddress)
        = readWord(bank, address) or (readByte(bank, shortAddress(address.shortAddress + 2)) shl 16)

    private fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        cycles += getCyclesForAddress(bank, address)
        memory.writeByte(bank, address, value)
    }

    private fun readFor(register: DiffSizeRegister, address: FullAddress)
        = if(register._8bitMode) readByte(address.bank, address.shortAddress) else readWord(address.bank, address.shortAddress)

    private fun writeWord(bank: Bank, address: ShortAddress, value: Int) {
        writeByte(bank, address, value)
        writeByte(bank, shortAddress(address.shortAddress + 1), value shr 8)
    }

    private fun writeLong(bank: Bank, address: ShortAddress, value: Int) {
        writeWord(bank, address, value)
        writeByte(bank, shortAddress(address.shortAddress + 2), value shr 16)
    }

    private fun writeFor(register: DiffSizeRegister, address: FullAddress, value: Int = register.value)
            = if (register._8bitMode) writeByte(address.bank, address.shortAddress, value) else writeWord(address.bank, address.shortAddress, value)

    private fun getCyclesForAddress(bank: Bank, address: ShortAddress): Int {
        // TODO copied from snes9x
        if (bank and 0x40 != 0 || address and 0x8000 != 0) {
            return if (bank and 0x80 != 0) FastROMSpeed else SLOW_ONE_CYCLE
        }

        if (address + 0x6000 and 0x4000 != 0) return SLOW_ONE_CYCLE

        return if (address - 0x4000 and 0x7e00 != 0) ONE_CYCLE else TWO_CYCLES

    }

    /**
     * the accumulator is a special 16-bit-register.
     * if it is not in 8-bit-mode it is a normal 16-bit-register.
     * if it is in 8-bit-mode, all set-operatioins preserve the upper byte, so that it can be used as temporary
     * storage using the XBA-isntruction
     */
    inner class Accumulator : DiffSizeRegister() {
        override fun shallBe8Bit(): Boolean {
            // 16-bit mode is in native mode if the memory-bit (M) is not set
            return mode == ProcessorMode.EMULATION || rP.accumulator
        }

        override fun checkValue(value: Int): Int {
            return if (_8bitMode) (this.value and 0xFF00) + (value and 0xFF) else super.checkValue(value)
        }

        fun xba() {
            value = ((value and 0xFF) shl 8) + ((value shr 8) and 0xFF)
        }
    }

    /**
     * an Index-register acts like a normal 8-bit register in 8-bit-mode and as a normal 16-bit-register else
     * so there nothing needed aside defining when to use which mode
     */
    inner class IndexRegister : DiffSizeRegister() {
        override fun shallBe8Bit(): Boolean {
            return mode == ProcessorMode.EMULATION || rP.index
        }
    }

    inner class StatusRegister(
        /** C */
        var carry: Boolean = false,
        /** Z */
        var zero: Boolean = false,
        /** I */
        var irqDisable: Boolean = false,
        /** D */
        var decimal: Boolean = false,
        /** X */
        var index: Boolean = false,
        /** M */
        var memory: Boolean = false,
        /** V */
        var overflow: Boolean = false,
        /** N */
        var negative: Boolean = false
    ) {
        val _break get() = index
        val accumulator get() = memory

        fun reset() {
            carry = false
            zero = false
            irqDisable = true
            decimal = false
            index = true
            memory = true
            overflow = false
            negative = false
        }

        fun asInt(): Int {
            var v = 0
            if (carry) v += BIT_CARRY
            if (zero) v += BIT_ZERO
            if (irqDisable) v += BIT_IRQ_DISABLE
            if (decimal) v += BIT_DECIMAL
            if (mode == ProcessorMode.EMULATION || index) v += BIT_INDEX
            if (mode == ProcessorMode.EMULATION || memory) v += BIT_MEMORY
            if (overflow) v += BIT_OVERFLOW
            if (negative) v += BIT_NEGATIVE

            // TODO in BRK-command in emulation mode M is not used (always 1) and X is B (1=BRK 0=IRQ)
            return v
        }

        fun fromInt(v: Int) {
            carry = v and BIT_CARRY != 0
            zero = v and BIT_ZERO != 0
            irqDisable = v and BIT_IRQ_DISABLE != 0
            decimal = v and BIT_DECIMAL != 0
            if(mode == ProcessorMode.EMULATION) {
                index = true
                memory = true
            } else {
                index = v and BIT_INDEX != 0
                memory = v and BIT_MEMORY != 0
            }
            overflow = v and BIT_OVERFLOW != 0
            negative = v and BIT_NEGATIVE != 0
        }
    }

    /**
     * The stack-register is a normal 16-bit-register. If 8-bit-mode is activated, the upper byte is
     * forced to always be one, so that it still is a valid 16-bit-register
     *
     * The bank-address for all stack-operations is always 0
     */
    inner class StackPointer : DiffSizeRegister() {
        override fun shallBe8Bit(): Boolean {
            return mode == ProcessorMode.EMULATION
        }

        override fun checkValue(value: Int): Int {
            // when in 8-bit-mode the upper byte is forced to always be 1
            return super.checkValue(value) + if (_8bitMode) 0x100 else 0
        }

        override fun reset() {
            super.reset()
            set(0xFF)
        }

        fun pushByte(value: Int) {
            writeByte(
                0,
                this.value,
                value)
            dec()
        }

        fun pushShort(value: Int) {
            pushByte(value shr 8)
            pushByte(value)
        }

        fun pullByte(): Int {
            inc()
            return readByte(
                0,
                value
            )
        }

        fun pullShort(): Int {
            return pullByte() + (pullByte() shl 8)
        }
    }

    private inner class Operands {
        /** a */
        val opAbsolute = operand("a", "absolute") {
            fullAddress(
                rDBR.value,
                fetchShort()
            )
        }
        /** (a, x) */
        val opAbsoluteIndexedIndirect = operand("(a, x)", "absolute indexed indirect") {
            fullAddress(
                rPBR.value,
                shortAddress(fetchShort() + rX)
            )
        }
        /** a,x */
        val opAbsoluteIndexedWithX = operand("a, x", "absolute indexed with x") {
            fullAddress(
                rDBR.value,
                shortAddress(fetchShort() + rX)
            )
        }
        /** a,y */
        val opAbsoluteIndexedWithY = operand("a, y", "absolute indexed with y") {
            fullAddress(
                rDBR.value,
                shortAddress(fetchShort() + rY)
            )
        }
        /** (a) */
        val opAbsoluteIndirect = operand("(a)", "absolute indirect") {
            fetchShort()
        }
        /** al,x */
        val opAbsoluteLongIndexedWithX = operand("al, x", "absolute long indexed with x") {
            fullAddress(fetchLong() + rX)
        }
        /** al */
        val opAbsoluteLong = operand("al", "absolute long") {
            fetchLong()
        }
        /** (d,x) */
        val opDirectIndexedIndirect = operand("(d, x)", "direct indexed indirect") {
            fullAddress(
                rDBR.value,
                readWord(
                    0,
                    shortAddress(fetch() + rD + rX)
                )
            )
        }
        /** d,x */
        val opDirectIndexedWithX = operand("d, x", "direct indexed with x") {
            shortAddress(fetch() + rD + rX)
        }
        /** d.y */
        val opDirectIndexedWithY = operand("d, y", "direct indexed with y") {
            shortAddress(fetch() + rD + rY)
        }
        /** (d),y */
        val opDirectIndirectIndexed = operand("(d), y", "direct indirect indexed") {
            fullAddress(
                fullAddress(
                    rDBR.value,
                    readWord(
                        0,
                        shortAddress(fetch() + rD)
                    )
                ) + rY
            )
        }
        /** \[d],y */
        val opDirectIndirectLongIndexed = operand("[d], y", "direct indirect long indexed") {
            fullAddress(
                readLong(
                    0,
                    shortAddress(fetch() + rD)
                ) + rY
            )
        }
        /** \[d] */
        val opDirectIndirectLong = operand("[d]", "direct indirect long") {
            readLong(
                0,
                shortAddress(fetch() + rD)
            )
        }
        /** (d) */
        val opDirectIndirect = operand("(d)", "direct indirect") {
            fullAddress(
                rDBR.value,
                readWord(
                    0,
                    shortAddress(fetch() + rD)
                )
            )
        }
        /** d */
        val opDirect = operand("d", "direct") {
            shortAddress(fetch() + rD)
        }
        /** # */
        val opImmediate = operand("#", "immediate") {
            fetch()
        }
        /** rl */
        // toShort converts it to signed, and toInt is needed for the calculation
        val opProgramCounterRelativeLong = operand("rl", "program counter relative long") {
            shortAddress(fetchShort().toShort().toInt() + rPC)
        }
        /** r */
        // toByte converts it to signed, and toInt is needed for the calculation
        val opProgramCounterRelative = operand("r", "program counter relative") {
            shortAddress(fetch().toByte().toInt() + rPC)
        }
        /** d,s */
        val opStackRelative = operand("d, s", "stack relative") {
            shortAddress(fetch() + rS)
        }
        /** (d,s),y */
        val opStackRelativeIndirectIndexed = operand("(d, s), y", "stack relative indirect indexed") {
            fullAddress(
                fullAddress(
                    rDBR.value,
                    readWord(
                        0,
                        shortAddress(fetch() + rS)
                    )
                ) + rY
            )
        }
        /** A */
        val opAccumulator = operand("A", "Accumulator") {
            rA.get()
        }
        /** xyc */
        val opBlockMove = operand("xyc", "Block move") {
            fetch()
        }

        private fun operand(symbol: String, name: String, getValue: () -> Int) = object : Operand(symbol, name) {
            override fun getValue(): Int {
                return getValue()
            }
        }

        // implied -i -> no further bytes used -> operand defined by instruction
        // stack -s
    }


    private inner class Instructions {
        private val instructions = arrayOf(
            /* 0x00 */ instructionS0("BRK", "Break-interrupt", this@Processor::instBRK),
            /* 0x01 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirectIndexedIndirect),
            /* 0x02 */ instructionS0("COP", "coprocessor-interrupt", this@Processor::instCOP),
            /* 0x03 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opStackRelative),
            /* 0x04 */ instructionWA("TSB", "Test and set Bit", this@Processor::instTSB, operands.opDirect),
            /* 0x05 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirect),
            /* 0x06 */ instructionWA("ASL", "Shift left", this@Processor::instASL, operands.opDirect),
            /* 0x07 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirectIndirectLong),
            /* 0x08 */ instructionS0("PHP", "Push P", this@Processor::instPHP),
            /* 0x09 */ instructionSI("ORA", "or with accumulator", this@Processor::instORA, operands.opImmediate, rA),
            /* 0x0A */ instructionSA("ASL", "Shift left", this@Processor::instASL),
            /* 0x0B */ instructionS0("PHD", "Push D", this@Processor::instPHD),
            /* 0x0C */ instructionWA("TSB", "Test and set Bit", this@Processor::instTSB, operands.opAbsolute),
            /* 0x0D */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opAbsolute),
            /* 0x0E */ instructionWA("ASL", "Shift left", this@Processor::instASL, operands.opAbsolute),
            /* 0x0F */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opAbsoluteLong),
            /* 0x10 */ instructionS1("BPL", "branch if plus", this@Processor::instBPL, operands.opProgramCounterRelative),
            /* 0x11 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirectIndirectIndexed),
            /* 0x12 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirectIndirect),
            /* 0x13 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opStackRelativeIndirectIndexed),
            /* 0x14 */ instructionWA("TRB", "Test and reset bit", this@Processor::instTRB, operands.opDirect),
            /* 0x15 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirectIndexedWithX),
            /* 0x16 */ instructionWA("ASL", "Shift left", this@Processor::instASL, operands.opDirectIndexedWithX),
            /* 0x17 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opDirectIndirectLongIndexed),
            /* 0x18 */ instructionS0("CLC", "Clear Carry", this@Processor::instCLC),
            /* 0x19 */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opAbsoluteIndexedWithY),
            /* 0x1A */ instructionSA("INC", "Increment", this@Processor::instINC),
            /* 0x1B */ instructionS0("TCS", "Transfer A to S", this@Processor::instTCS),
            /* 0x1C */ instructionWA("TRB", "Test and reset bit", this@Processor::instTRB, operands.opAbsolute),
            /* 0x1D */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opAbsoluteIndexedWithX),
            /* 0x1E */ instructionWA("ASL", "Shift left", this@Processor::instASL, operands.opAbsoluteIndexedWithX),
            /* 0x1F */ instructionRA("ORA", "or with accumulator", this@Processor::instORA, operands.opAbsoluteLongIndexedWithX),
            /* 0x20 */ instructionS1("JSR", "Jump to subroutine", this@Processor::instJSR, operands.opAbsolute),
            /* 0x21 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirectIndexedIndirect),
            /* 0x22 */ instructionS1("JSL", "Jump to subroutine long", this@Processor::instJSL, operands.opAbsoluteLong),
            /* 0x23 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opStackRelative),
            /* 0x24 */ instructionRA("BIT", "Bit test between A and the value", this@Processor::instBIT, operands.opDirect),
            /* 0x25 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirect),
            /* 0x26 */ instructionWA("ROL", "shift left with carry", this@Processor::instROL, operands.opDirect),
            /* 0x27 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirectIndirectLong),
            /* 0x28 */ instructionS0("PLP", "Pull P", this@Processor::instPLP),
            /* 0x29 */ instructionSI("AND", "and with accumulator", this@Processor::instAND, operands.opImmediate, rA),
            /* 0x2A */ instructionSA("ROL", "shift left with carry", this@Processor::instROL),
            /* 0x2B */ instructionS0("PLD", "Pull D", this@Processor::instPLD),
            /* 0x2C */ instructionRA("BIT", "Bit test between A and the value", this@Processor::instBIT, operands.opAbsolute),
            /* 0x2D */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opAbsolute),
            /* 0x2E */ instructionWA("ROL", "shift left with carry", this@Processor::instROL, operands.opAbsolute),
            /* 0x2F */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opAbsoluteLong),
            /* 0x30 */ instructionS1("BMI", "Branch if Minus", this@Processor::instBMI, operands.opProgramCounterRelative),
            /* 0x31 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirectIndirectIndexed),
            /* 0x32 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirectIndirect),
            /* 0x33 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opStackRelativeIndirectIndexed),
            /* 0x34 */ instructionRA("BIT", "Bit test between A and the value", this@Processor::instBIT, operands.opDirectIndexedWithX),
            /* 0x35 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirectIndexedWithX),
            /* 0x36 */ instructionWA("ROL", "shift left with carry", this@Processor::instROL, operands.opDirectIndexedWithX),
            /* 0x37 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opDirectIndirectLongIndexed),
            /* 0x38 */ instructionS0("SEC", "Set Carry", this@Processor::instSEC),
            /* 0x39 */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opAbsoluteIndexedWithY),
            /* 0x3A */ instructionSA("DEC", "Decrement", this@Processor::instDEC),
            /* 0x3B */ instructionS0("TSC", "Transfer S to A", this@Processor::instTSC),
            /* 0x3C */ instructionRA("BIT", "Bit test between A and the value", this@Processor::instBIT, operands.opAbsoluteIndexedWithX),
            /* 0x3D */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opAbsoluteIndexedWithX),
            /* 0x3E */ instructionWA("ROL", "shift left with carry", this@Processor::instROL, operands.opAbsoluteIndexedWithX),
            /* 0x3F */ instructionRA("AND", "and with accumulator", this@Processor::instAND, operands.opAbsoluteLongIndexedWithX),
            /* 0x40 */ instructionS0("RTI", "Return from interrupt", this@Processor::instRTI),
            /* 0x41 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirectIndexedIndirect),
            /* 0x42 */ instructionS0("WDM", "Reserved for future use", this@Processor::instWDM),
            /* 0x43 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opStackRelative),
            /* 0x44 */ instructionS0("MVP", "Block move positive", this@Processor::instMVP),
            /* 0x45 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirect),
            /* 0x46 */ instructionWA("LSR", "shift right", this@Processor::instLSR, operands.opDirect),
            /* 0x47 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirectIndirectLong),
            /* 0x48 */ instructionS0("PHA", "Push A", this@Processor::instPHA),
            /* 0x49 */ instructionSI("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opImmediate, rA),
            /* 0x4A */ instructionSA("LSR", "shift right", this@Processor::instLSR),
            /* 0x4B */ instructionS0("PHK", "Push PBR", this@Processor::instPHK),
            /* 0x4C */ instructionS1("JMP", "Jump to full address", this@Processor::instJMP, operands.opAbsolute),
            /* 0x4D */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opAbsolute),
            /* 0x4E */ instructionWA("LSR", "shift right", this@Processor::instLSR, operands.opAbsolute),
            /* 0x4F */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opAbsoluteLong),
            /* 0x50 */ instructionS1("BVC", "Branch if overflow clear", this@Processor::instBVC, operands.opProgramCounterRelative),
            /* 0x51 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirectIndirectIndexed),
            /* 0x52 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirectIndirect),
            /* 0x53 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opStackRelativeIndirectIndexed),
            /* 0x54 */ instructionS0("MVN", "Block move negative", this@Processor::instMVN),
            /* 0x55 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirectIndexedWithX),
            /* 0x56 */ instructionWA("LSR", "shift right", this@Processor::instLSR, operands.opDirectIndexedWithX),
            /* 0x57 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opDirectIndirectLongIndexed),
            /* 0x58 */ instructionS0("CLI", "Clear irq/interrupt flag", this@Processor::instCLI),
            /* 0x59 */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opAbsoluteIndexedWithY),
            /* 0x5A */ instructionS0("PHY", "Push Y", this@Processor::instPHY),
            /* 0x5B */ instructionS0("TCD", "Transfer A to D", this@Processor::instTCD),
            /* 0x5C */ instructionS1("JML", "Jump to address", this@Processor::instJML, operands.opAbsoluteLong),
            /* 0x5D */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opAbsoluteIndexedWithX),
            /* 0x5E */ instructionWA("LSR", "shift right", this@Processor::instLSR, operands.opAbsoluteIndexedWithX),
            /* 0x5F */ instructionRA("EOR", "exclusive or with accumulator", this@Processor::instEOR, operands.opAbsoluteLongIndexedWithX),
            /* 0x60 */ instructionS0("RTS", "Return from Subroutine", this@Processor::instRTS),
            /* 0x61 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirectIndexedIndirect),
            /* 0x62 */ instructionS1("PER", "Push relative address", this@Processor::instPER, operands.opProgramCounterRelativeLong),
            /* 0x63 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opStackRelative),
            /* 0x64 */ instructionS1("STZ", "Store zero", this@Processor::instSTZ, operands.opDirect),
            /* 0x65 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirect),
            /* 0x66 */ instructionWA("ROR", "shift right with carry", this@Processor::instROR, operands.opDirect),
            /* 0x67 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirectIndirectLong),
            /* 0x68 */ instructionS0("PLA", "Pull A", this@Processor::instPLA),
            /* 0x69 */ instructionSI("ADC", "Add with carry", this@Processor::instADC, operands.opImmediate, rA),
            /* 0x6A */ instructionSA("ROR", "shift right with carry", this@Processor::instROR),
            /* 0x6B */ instructionS0("RTL", "Return from subroutine long", this@Processor::instRTL),
            /* 0x6C */ instructionS1("JMP", "Jump to address", this@Processor::instJMP, operands.opAbsoluteIndirect),
            /* 0x6D */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opAbsolute),
            /* 0x6E */ instructionWA("ROR", "shift right with carry", this@Processor::instROR, operands.opAbsolute),
            /* 0x6F */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opAbsoluteLong),
            /* 0x70 */ instructionS1("BVS", "Branch if overflow set", this@Processor::instBVS, operands.opProgramCounterRelative),
            /* 0x71 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirectIndirectIndexed),
            /* 0x72 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirectIndirect),
            /* 0x73 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opStackRelativeIndirectIndexed),
            /* 0x74 */ instructionS1("STZ", "Store Zero", this@Processor::instSTZ, operands.opDirectIndexedWithX),
            /* 0x75 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirectIndexedWithX),
            /* 0x76 */ instructionWA("ROR", "shift right with carry", this@Processor::instROR, operands.opDirectIndexedWithX),
            /* 0x77 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opDirectIndirectLongIndexed),
            /* 0x78 */ instructionS0("SEI", "Set irq/interrupt flag", this@Processor::instSEI),
            /* 0x79 */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opAbsoluteIndexedWithY),
            /* 0x7A */ instructionS0("PLY", "Pull Y", this@Processor::instPLY),
            /* 0x7B */ instructionS0("TDC", "Transfer D to A", this@Processor::instTDC),
            /* 0x7C */ instructionS1("JMP", "Jump to address", this@Processor::instJMP, operands.opAbsoluteIndexedIndirect),
            /* 0x7D */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opAbsoluteIndexedWithX),
            /* 0x7E */ instructionWA("ROR", "shift right with carry", this@Processor::instROR, operands.opAbsoluteIndexedWithX),
            /* 0x7F */ instructionRA("ADC", "Add with carry", this@Processor::instADC, operands.opAbsoluteLongIndexedWithX),
            /* 0x80 */ instructionS1("BRA", "Branch always", this@Processor::instBRA, operands.opProgramCounterRelative),
            /* 0x81 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirectIndexedIndirect),
            /* 0x82 */ instructionS1("BRL", "Branch always long", this@Processor::instBRL, operands.opProgramCounterRelativeLong),
            /* 0x83 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opStackRelative),
            /* 0x84 */ instructionS1("STY", "Store Y", this@Processor::instSTY, operands.opDirect),
            /* 0x85 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirect),
            /* 0x86 */ instructionS1("STX", "Store X", this@Processor::instSTX, operands.opDirect),
            /* 0x87 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirectIndirectLong),
            /* 0x88 */ instructionS0("DEY", "Decrement Y", this@Processor::instDEY),
            /* 0x89 */ instructionSI("BIT", "Bit test between A and the value", this@Processor::instBITImmediate, operands.opImmediate, rA),
            /* 0x8A */ instructionS0("TXA", "Transfer X to A", this@Processor::instTXA),
            /* 0x8B */ instructionS0("PHB", "Push DBR", this@Processor::instPHB),
            /* 0x8C */ instructionS1("STY", "Store Y", this@Processor::instSTY, operands.opAbsolute),
            /* 0x8D */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opAbsolute),
            /* 0x8E */ instructionS1("STX", "Store X", this@Processor::instSTX, operands.opAbsolute),
            /* 0x8F */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opAbsoluteLong),
            /* 0x90 */ instructionS1("BCC", "Branch if carry clear", this@Processor::instBCC, operands.opProgramCounterRelative),
            /* 0x91 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirectIndirectIndexed),
            /* 0x92 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirectIndirect),
            /* 0x93 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opStackRelativeIndirectIndexed),
            /* 0x94 */ instructionS1("STY", "Store Y", this@Processor::instSTY, operands.opDirectIndexedWithX),
            /* 0x95 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirectIndexedWithX),
            /* 0x96 */ instructionS1("STX", "Store X", this@Processor::instSTX, operands.opDirectIndexedWithY),
            /* 0x97 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opDirectIndirectLongIndexed),
            /* 0x98 */ instructionS0("TYA", "Transfer Y to A", this@Processor::instTYA),
            /* 0x99 */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opAbsoluteIndexedWithY),
            /* 0x9A */ instructionS0("TXS", "Transfer X to S", this@Processor::instTXS),
            /* 0x9B */ instructionS0("TXY", "Transfer X to Y", this@Processor::instTXY),
            /* 0x9C */ instructionS1("STZ", "Store Zero", this@Processor::instSTZ, operands.opAbsolute),
            /* 0x9D */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opAbsoluteIndexedWithX),
            /* 0x9E */ instructionS1("STZ", "Store Zero", this@Processor::instSTZ, operands.opAbsoluteIndexedWithX),
            /* 0x9F */ instructionS1("STA", "Store A", this@Processor::instSTA, operands.opAbsoluteLongIndexedWithX),
            /* 0xA0 */ instructionSI("LDY", "Load Y", this@Processor::instLDY, operands.opImmediate, rY),
            /* 0xA1 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirectIndexedIndirect),
            /* 0xA2 */ instructionSI("LDX", "Load X", this@Processor::instLDX, operands.opImmediate, rX),
            /* 0xA3 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opStackRelative),
            /* 0xA4 */ instructionRY("LDY", "Load Y", this@Processor::instLDY, operands.opDirect),
            /* 0xA5 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirect),
            /* 0xA6 */ instructionRX("LDX", "Load X", this@Processor::instLDX, operands.opDirect),
            /* 0xA7 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirectIndirectLong),
            /* 0xA8 */ instructionS0("TAY", "Transfer A to Y", this@Processor::instTAY),
            /* 0xA9 */ instructionSI("LDA", "Load A", this@Processor::instLDA, operands.opImmediate, rA),
            /* 0xAA */ instructionS0("TAX", "Transfer A to X", this@Processor::instTAX),
            /* 0xAB */ instructionS0("PLB", "Pull DBR", this@Processor::instPLB),
            /* 0xAC */ instructionRY("LDY", "Load Y", this@Processor::instLDY, operands.opAbsolute),
            /* 0xAD */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opAbsolute),
            /* 0xAE */ instructionRX("LDX", "Load X", this@Processor::instLDX, operands.opAbsolute),
            /* 0xAF */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opAbsoluteLong),
            /* 0xB0 */ instructionS1("BCS", "Branch if carry set", this@Processor::instBCS, operands.opProgramCounterRelative),
            /* 0xB1 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirectIndirectIndexed),
            /* 0xB2 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirectIndirect),
            /* 0xB3 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opStackRelativeIndirectIndexed),
            /* 0xB4 */ instructionRY("LDY", "Load Y", this@Processor::instLDY, operands.opDirectIndexedWithX),
            /* 0xB5 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirectIndexedWithX),
            /* 0xB6 */ instructionRX("LDX", "Load X", this@Processor::instLDX, operands.opDirectIndexedWithY),
            /* 0xB7 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opDirectIndirectLongIndexed),
            /* 0xB8 */ instructionS0("CLV", "Clear overflow flag", this@Processor::instCLV),
            /* 0xB9 */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opAbsoluteIndexedWithY),
            /* 0xBA */ instructionS0("TSX", "Transfer S to X", this@Processor::instTSX),
            /* 0xBB */ instructionS0("TYX", "Transfer Y to X", this@Processor::instTYX),
            /* 0xBC */ instructionRY("LDY", "Load Y", this@Processor::instLDY, operands.opAbsoluteIndexedWithX),
            /* 0xBD */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opAbsoluteIndexedWithX),
            /* 0xBE */ instructionRX("LDX", "Load X", this@Processor::instLDX, operands.opAbsoluteIndexedWithY),
            /* 0xBF */ instructionRA("LDA", "Load A", this@Processor::instLDA, operands.opAbsoluteLongIndexedWithX),
            /* 0xC0 */ instructionSI("CPY", "Compare value with Y", this@Processor::instCPY, operands.opImmediate, rY),
            /* 0xC1 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirectIndexedIndirect),
            /* 0xC2 */ instructionSI("REP", "Reset Bit in P", this@Processor::instREP, operands.opImmediate, 1),
            /* 0xC3 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opStackRelative),
            /* 0xC4 */ instructionRY("CPY", "Compare value with Y", this@Processor::instCPY, operands.opDirect),
            /* 0xC5 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirect),
            /* 0xC6 */ instructionWA("DEC", "Decrement", this@Processor::instDEC, operands.opDirect),
            /* 0xC7 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirectIndirectLong),
            /* 0xC8 */ instructionS0("INY", "Increment Y", this@Processor::instINY),
            /* 0xC9 */ instructionSI("CMP", "Compare value with A", this@Processor::instCMP, operands.opImmediate, rA),
            /* 0xCA */ instructionS0("DEX", "Decrement X", this@Processor::instDEX),
            /* 0xCB */ instructionS0("WAI", "Wait for interrupt", this@Processor::instWAI),
            /* 0xCC */ instructionRY("CPY", "Compare value with Y", this@Processor::instCPY, operands.opAbsolute),
            /* 0xCD */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opAbsolute),
            /* 0xCE */ instructionWA("DEC", "Decrement", this@Processor::instDEC, operands.opAbsolute),
            /* 0xCF */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opAbsoluteLong),
            /* 0xD0 */ instructionS1("BNE", "Branch if not equal/zero clear", this@Processor::instBNE, operands.opProgramCounterRelative),
            /* 0xD1 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirectIndirectIndexed),
            /* 0xD2 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirectIndirect),
            /* 0xD3 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opStackRelativeIndirectIndexed),
            /* 0xD4 */ instructionS1("PEI", "Push effective indirect address", this@Processor::instPEI, operands.opDirect),
            /* 0xD5 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirectIndexedWithX),
            /* 0xD6 */ instructionWA("DEC", "Decrement", this@Processor::instDEC, operands.opDirectIndexedWithX),
            /* 0xD7 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opDirectIndirectLongIndexed),
            /* 0xD8 */ instructionS0("CLD", "Clear decimal flag", this@Processor::instCLD),
            /* 0xD9 */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opAbsoluteIndexedWithY),
            /* 0xDA */ instructionS0("PHX", "Push X", this@Processor::instPHX),
            /* 0xDB */ instructionS0("STP", "Stop the clock", this@Processor::instSTP),
            /* 0xDC */ instructionS1("JML", "Jump to address Long", this@Processor::instJML, operands.opAbsoluteIndirect),
            /* 0xDD */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opAbsoluteIndexedWithX),
            /* 0xDE */ instructionWA("DEC", "Decrement", this@Processor::instDEC, operands.opAbsoluteIndexedWithX),
            /* 0xDF */ instructionRA("CMP", "Compare value with A", this@Processor::instCMP, operands.opAbsoluteLongIndexedWithX),
            /* 0xE0 */ instructionSI("CPX", "Compare value with X", this@Processor::instCPX, operands.opImmediate, rX),
            /* 0xE1 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirectIndexedWithX),
            /* 0xE2 */ instructionSI("SEP", "Set Bit in P", this@Processor::instSEP, operands.opImmediate, 1),
            /* 0xE3 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opStackRelative),
            /* 0xE4 */ instructionRX("CPX", "Compare value with X", this@Processor::instCPX, operands.opDirect),
            /* 0xE5 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirect),
            /* 0xE6 */ instructionWA("INC", "Increment", this@Processor::instINC, operands.opDirect),
            /* 0xE7 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirectIndirectLong),
            /* 0xE8 */ instructionS0("INX", "Increment X", this@Processor::instINX),
            /* 0xE9 */ instructionSI("SBC", "Subtract with carry", this@Processor::instSBC, operands.opImmediate, rA),
            /* 0xEA */ instructionS0("NOP", "No Operation", this@Processor::instNOP),
            /* 0xEB */ instructionS0("XBA", "Exchange B and A", this@Processor::instXBA),
            /* 0xEC */ instructionRX("CPX", "Compare value with X", this@Processor::instCPX, operands.opAbsolute),
            /* 0xED */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opAbsolute),
            /* 0xEE */ instructionWA("INC", "Increment", this@Processor::instINC, operands.opAbsolute),
            /* 0xEF */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opAbsoluteLong),
            /* 0xF0 */ instructionS1("BEQ", "Branch if zero set/equal", this@Processor::instBEQ, operands.opProgramCounterRelative),
            /* 0xF1 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirectIndirectIndexed),
            /* 0xF2 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirectIndirect),
            /* 0xF3 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opStackRelativeIndirectIndexed),
            /* 0xF4 */ instructionSI("PEA", "Push effective address", this@Processor::instPEA, operands.opImmediate, 2),
            /* 0xF5 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirectIndexedWithX),
            /* 0xF6 */ instructionWA("INC", "Increment", this@Processor::instINC, operands.opDirectIndexedWithX),
            /* 0xF7 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opDirectIndirectLongIndexed),
            /* 0xF8 */ instructionS0("SED", "Set decimal flag", this@Processor::instSED),
            /* 0xF9 */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opAbsoluteIndexedWithY),
            /* 0xFA */ instructionS0("PLX", "Pull X", this@Processor::instPLX),
            /* 0xFB */ instructionS0("XCE", "Exchange Carry and Emulation flag", this@Processor::instXCE),
            /* 0xFC */ instructionS1("JSR", "Jump to subroutine", this@Processor::instJSR, operands.opAbsoluteIndexedIndirect),
            /* 0xFD */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opAbsoluteIndexedWithX),
            /* 0xFE */ instructionWA("INC", "Increment", this@Processor::instINC, operands.opAbsoluteIndexedWithX),
            /* 0xFF */ instructionRA("SBC", "Subtract with carry", this@Processor::instSBC, operands.opAbsoluteLongIndexedWithX)
        )

        /** simple instruction, no operand, no return, no read, no write */
        private fun instructionS0(symbol: String, description: String, action: () -> Any) = object :  Instruction(symbol, description) {
            override fun execute() {
                action()
            }
        }

        /** simple instruction with one operand, no return, no read, no write */
        private fun instructionS1(symbol: String, description: String, action: (Int) -> Unit, operand: Operand) = object : Instruction1(symbol, description, operand) {
            override fun execute() {
                action(operand())
            }
        }

        /** instruction with read from address, the address is given by the operand and the read-size is determined by the given register */
        private fun instructionRR(
            symbol: String,
            description: String,
            action: (Int) -> Unit,
            operand: Operand,
            register: DiffSizeRegister
        )= object : Instruction1(symbol, description, operand) {
            override fun execute() {
                action(readFor(register, operand()))
            }
        }

        /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by the given register */
        private fun instructionWR(
            symbol: String,
            description: String,
            action: (Int) -> Int,
            operand: Operand,
            register: DiffSizeRegister
        ) = object : Instruction1(symbol, description, operand) {
            override fun execute() {
                operand().let { writeFor(register, it, action(readFor(register, it))) }
            }
        }

        /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by the given register */
        private fun instructionWR(
            symbol: String,
            description: String,
            action: (DiffSizeRegister, Int) -> Int,
            operand: Operand,
            register: DiffSizeRegister
        ) = object : Instruction1(symbol, description, operand) {
            override fun execute() {
                operand().let { writeFor(register, it, action(register, readFor(register, it))) }
            }
        }

        /** instruction which reads the operand from A and sets A */
        private fun instructionSR(symbol: String, description: String, action: (Int) -> Int, register: Register) = object : Instruction(symbol, description) {
            override fun execute() {
                register.set(action(register.get()))
            }
        }

        /** instruction which reads the operand from A and sets A */
        private fun <R : Register> instructionSR(symbol: String, description: String, action: (R, Int) -> Int, register: R) = object : Instruction(symbol, description) {
            override fun execute() {
                register.set(action(register, register.get()))
            }
        }


        /** simple instruction with one operand, no return, no read, no write, this is only used for immediate operand to define how many bytes to read */
        private fun instructionSI(
            symbol: String,
            description: String,
            action: (Int) -> Unit,
            operand: Operand,
            register: DiffSizeRegister
        ) = object : Instruction(symbol, description) {
            override fun execute() {
                if (register._8bitMode) {
                    action(operand())
                } else {
                    action(operand() or (operand() shl 8))
                }
            }
        }

        /** simple instruction with one operand, no return, no read, no write , this is only used for immediate operand to define how many bytes to read*/
        private fun instructionSI(
            symbol: String,
            description: String,
            action: (Int) -> Unit,
            operand: Operand,
            arg: Int
        ) = object : Instruction(symbol, description) {
            override fun execute() {
                if (arg == 1) {
                    action(operand())
                } else {
                    action(operand() or (operand() shl 8))
                }
            }
        }

        /** instruction with read from address, the address is given by the operand and the read-size is determined by A */
        private fun instructionRA(symbol: String, description: String, action: (Int) -> Unit, operand: Operand) =
            instructionRR(symbol, description, action, operand, rA)

        /** instruction with read from address, the address is given by the operand and the read-size is determined by X */
        private fun instructionRX(symbol: String, description: String, action: (Int) -> Unit, operand: Operand) =
            instructionRR(symbol, description, action, operand, rX)

        /** instruction with read from address, the address is given by the operand and the read-size is determined by Y */
        private fun instructionRY(symbol: String, description: String, action: (Int) -> Unit, operand: Operand) =
            instructionRR(symbol, description, action, operand, rY)

        /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by A */
        private fun instructionWA(symbol: String, description: String, action: (Int) -> Int, operand: Operand) =
            instructionWR(symbol, description, action, operand, rA)

        /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by A */
        private fun instructionWA(
            symbol: String,
            description: String,
            action: (DiffSizeRegister, Int) -> Int,
            operand: Operand
        ) = instructionWR(symbol, description, action, operand, rA)

        /** instruction which reads the operand from A and sets A */
        private fun instructionSA(symbol: String, description: String, action: (Int) -> Int) = instructionSR(symbol, description, action, rA)

        /** instruction which reads the operand from A and sets A */
        private fun instructionSA(symbol: String, description: String, action: (DiffSizeRegister, Int) -> Int) = instructionSR(symbol, description, action, rA)

        operator fun get(index: Int) = instructions[index]
    }


    companion object {
        private const val BIT_CARRY = 0x01
        private const val BIT_ZERO = 0x02
        private const val BIT_IRQ_DISABLE = 0x04
        private const val BIT_DECIMAL = 0x08
        private const val BIT_INDEX = 0x10 // may change size of index-register from 8 to 16 bit
        private const val BIT_MEMORY = 0x20
        private const val BIT_OVERFLOW = 0x40
        private const val BIT_NEGATIVE = 0x80

        private const val BIT_BREAK = BIT_INDEX

        private const val COP_VECTOR_ADDRESS: ShortAddress = 0xFFF4
        private const val NATIVE_BRK_VECTOR_ADDRESS: ShortAddress = 0xFFF6
        private const val ABORT_VECTOR_ADDRESS: ShortAddress = 0xFFF8
        private const val NMI_VECTOR_ADDRESS: ShortAddress = 0xFFFA
        private const val RESET_VECTOR_ADDRESS: ShortAddress = 0xFFFC
        private const val IRQ_VECTOR_ADDRESS: ShortAddress = 0xFFFE
        private const val EMULATION_BRK_VECTOR_ADDRESS: ShortAddress = IRQ_VECTOR_ADDRESS

        private operator fun Int.plus(s: StackPointer) = if(s._8bitMode) this + s.value and 0xFF else this + s.value
        private operator fun Int.plus(a: Accumulator) = if (a._8bitMode) this + a.value and 0xFF else this + a.value
        private operator fun Int.plus(r: Register) = this + r.value

        private const val ONE_CYCLE = 6
        private const val TWO_CYCLES = 2 * ONE_CYCLE
        private const val SLOW_ONE_CYCLE = 8
        private const val FastROMSpeed = ONE_CYCLE // TODO depending on bit 0 in 420D this is ONE_CYCLE or SLOW_ONE_CYCLE
    }
}
