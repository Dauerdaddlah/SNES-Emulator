package de.dde.snes.processor

import de.dde.snes.*
import de.dde.snes.memory.*


class Processor(
    val memory: Memory
) {
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

    private val instructions = arrayOf(
        /* 0x00 */ instructionSi(this::instBRK),
        /* 0x01 */ instructionRA(this::instORA, this::opDirectIndexedIndirect),
        /* 0x02 */ instructionSi(this::instCOP),
        /* 0x03 */ instructionRA(this::instORA, this::opStackRelative),
        /* 0x04 */ instructionWA(this::instTSB, this::opDirect),
        /* 0x05 */ instructionRA(this::instORA, this::opDirect),
        /* 0x06 */ instructionWA(this::instASL, this::opDirect),
        /* 0x07 */ instructionRA(this::instORA, this::opDirectIndirectLong),
        /* 0x08 */ instructionSi(this::instPHP),
        /* 0x09 */ instructionSi(this::instORA, this::opImmediate, rA),
        /* 0x0A */ instructionSA(this::instASL),
        /* 0x0B */ instructionSi(this::instPHD),
        /* 0x0C */ instructionWA(this::instTSB, this::opAbsolute),
        /* 0x0D */ instructionRA(this::instORA, this::opAbsolute),
        /* 0x0E */ instructionWA(this::instASL, this::opAbsolute),
        /* 0x0F */ instructionRA(this::instORA, this::opAbsoluteLong),
        /* 0x10 */ instructionSi(this::instBPL, this::opProgramCounterRelative),
        /* 0x11 */ instructionRA(this::instORA, this::opDirectIndirectIndexed),
        /* 0x12 */ instructionRA(this::instORA, this::opDirectIndirect),
        /* 0x13 */ instructionRA(this::instORA, this::opStackRelativeIndirectIndexed),
        /* 0x14 */ instructionWA(this::instTRB, this::opDirect),
        /* 0x15 */ instructionRA(this::instORA, this::opDirectIndexedWithX),
        /* 0x16 */ instructionWA(this::instASL, this::opDirectIndexedWithX),
        /* 0x17 */ instructionRA(this::instORA, this::opDirectIndirectLongIndexed),
        /* 0x18 */ instructionSi(this::instCLC),
        /* 0x19 */ instructionRA(this::instORA, this::opAbsoluteIndexedWithY),
        /* 0x1A */ instructionSA(this::instINC),
        /* 0x1B */ instructionSi(this::instTCS),
        /* 0x1C */ instructionWA(this::instTRB, this::opAbsolute),
        /* 0x1D */ instructionRA(this::instORA, this::opAbsoluteIndexedWithX),
        /* 0x1E */ instructionWA(this::instASL, this::opAbsoluteIndexedWithX),
        /* 0x1F */ instructionRA(this::instORA, this::opAbsoluteLongIndexedWithX),
        /* 0x20 */ instructionSi(this::instJSR, this::opAbsolute),
        /* 0x21 */ instructionRA(this::instAND, this::opDirectIndexedIndirect),
        /* 0x22 */ instructionSi(this::instJSL, this::opAbsoluteLong),
        /* 0x23 */ instructionRA(this::instAND, this::opStackRelative),
        /* 0x24 */ instructionRA(this::instBIT, this::opDirect),
        /* 0x25 */ instructionRA(this::instAND, this::opDirect),
        /* 0x26 */ instructionWA(this::instROL, this::opDirect),
        /* 0x27 */ instructionRA(this::instAND, this::opDirectIndirectLong),
        /* 0x28 */ instructionSi(this::instPLP),
        /* 0x29 */ instructionSi(this::instAND, this::opImmediate, rA),
        /* 0x2A */ instructionSA(this::instROL),
        /* 0x2B */ instructionSi(this::instPLD),
        /* 0x2C */ instructionRA(this::instBIT, this::opAbsolute),
        /* 0x2D */ instructionRA(this::instAND, this::opAbsolute),
        /* 0x2E */ instructionWA(this::instROL, this::opAbsolute),
        /* 0x2F */ instructionRA(this::instAND, this::opAbsoluteLong),
        /* 0x30 */ instructionSi(this::instBMI, this::opProgramCounterRelative),
        /* 0x31 */ instructionRA(this::instAND, this::opDirectIndirectIndexed),
        /* 0x32 */ instructionRA(this::instAND, this::opDirectIndirect),
        /* 0x33 */ instructionRA(this::instAND, this::opStackRelativeIndirectIndexed),
        /* 0x34 */ instructionRA(this::instBIT, this::opDirectIndexedWithX),
        /* 0x35 */ instructionRA(this::instAND, this::opDirectIndexedWithX),
        /* 0x36 */ instructionWA(this::instROL, this::opDirectIndexedWithX),
        /* 0x37 */ instructionRA(this::instAND, this::opDirectIndirectLongIndexed),
        /* 0x38 */ instructionSi(this::instSEC),
        /* 0x39 */ instructionRA(this::instAND, this::opAbsoluteIndexedWithY),
        /* 0x3A */ instructionSA(this::instDEC),
        /* 0x3B */ instructionSi(this::instTSC),
        /* 0x3C */ instructionRA(this::instBIT, this::opAbsoluteIndexedWithX),
        /* 0x3D */ instructionRA(this::instAND, this::opAbsoluteIndexedWithX),
        /* 0x3E */ instructionWA(this::instROL, this::opAbsoluteIndexedWithX),
        /* 0x3F */ instructionRA(this::instAND, this::opAbsoluteLongIndexedWithX),
        /* 0x40 */ instructionSi(this::instRTI),
        /* 0x41 */ instructionRA(this::instEOR, this::opDirectIndexedIndirect),
        /* 0x42 */ instructionSi(this::instWDM),
        /* 0x43 */ instructionRA(this::instEOR, this::opStackRelative),
        /* 0x44 */ instructionSi(this::instMVP),
        /* 0x45 */ instructionRA(this::instEOR, this::opDirect),
        /* 0x46 */ instructionWA(this::instLSR, this::opDirect),
        /* 0x47 */ instructionRA(this::instEOR, this::opDirectIndirectLong),
        /* 0x48 */ instructionSi(this::instPHA),
        /* 0x49 */ instructionSi(this::instEOR, this::opImmediate, rA),
        /* 0x4A */ instructionSA(this::instLSR),
        /* 0x4B */ instructionSi(this::instPHK),
        /* 0x4C */ instructionSi(this::instJMP, this::opAbsolute),
        /* 0x4D */ instructionRA(this::instEOR, this::opAbsolute),
        /* 0x4E */ instructionWA(this::instLSR, this::opAbsolute),
        /* 0x4F */ instructionRA(this::instEOR, this::opAbsoluteLong),
        /* 0x50 */ instructionSi(this::instBVC, this::opProgramCounterRelative),
        /* 0x51 */ instructionRA(this::instEOR, this::opDirectIndirectIndexed),
        /* 0x52 */ instructionRA(this::instEOR, this::opDirectIndirect),
        /* 0x53 */ instructionRA(this::instEOR, this::opStackRelativeIndirectIndexed),
        /* 0x54 */ instructionSi(this::instMVN),
        /* 0x55 */ instructionRA(this::instEOR, this::opDirectIndexedWithX),
        /* 0x56 */ instructionWA(this::instLSR, this::opDirectIndexedWithX),
        /* 0x57 */ instructionRA(this::instEOR, this::opDirectIndirectLongIndexed),
        /* 0x58 */ instructionSi(this::instCLI),
        /* 0x59 */ instructionRA(this::instEOR, this::opAbsoluteIndexedWithY),
        /* 0x5A */ instructionSi(this::instPHY),
        /* 0x5B */ instructionSi(this::instTCD),
        /* 0x5C */ instructionSi(this::instJMP, this::opAbsoluteLong),
        /* 0x5D */ instructionRA(this::instEOR, this::opAbsoluteIndexedWithX),
        /* 0x5E */ instructionWA(this::instLSR, this::opAbsoluteIndexedWithX),
        /* 0x5F */ instructionRA(this::instEOR, this::opAbsoluteLongIndexedWithX),
        /* 0x60 */ instructionSi(this::instRTS),
        /* 0x61 */ instructionRA(this::instADC, this::opDirectIndexedIndirect),
        /* 0x62 */ instructionSi(this::instPER),
        /* 0x63 */ instructionRA(this::instADC, this::opStackRelative),
        /* 0x64 */ instructionSi(this::instSTZ, this::opDirect),
        /* 0x65 */ instructionRA(this::instADC, this::opDirect),
        /* 0x66 */ instructionWA(this::instROR, this::opDirect),
        /* 0x67 */ instructionRA(this::instADC, this::opDirectIndirectLong),
        /* 0x68 */ instructionSi(this::instPLA),
        /* 0x69 */ instructionSi(this::instADC, this::opImmediate, rA),
        /* 0x6A */ instructionSA(this::instROR),
        /* 0x6B */ instructionSi(this::instRTL),
        /* 0x6C */ instructionSi(this::instJMP, this::opAbsoluteIndirect),
        /* 0x6D */ instructionRA(this::instADC, this::opAbsolute),
        /* 0x6E */ instructionWA(this::instROR, this::opAbsolute),
        /* 0x6F */ instructionRA(this::instADC, this::opAbsoluteLong),
        /* 0x70 */ instructionSi(this::instBVS, this::opProgramCounterRelative),
        /* 0x71 */ instructionRA(this::instADC, this::opDirectIndirectIndexed),
        /* 0x72 */ instructionRA(this::instADC, this::opDirectIndirect),
        /* 0x73 */ instructionRA(this::instADC, this::opStackRelativeIndirectIndexed),
        /* 0x74 */ instructionSi(this::instSTZ, this::opDirectIndexedWithX),
        /* 0x75 */ instructionRA(this::instADC, this::opDirectIndexedWithX),
        /* 0x76 */ instructionWA(this::instROR, this::opDirectIndexedWithX),
        /* 0x77 */ instructionRA(this::instADC, this::opDirectIndirectLongIndexed),
        /* 0x78 */ instructionSi(this::instSEI),
        /* 0x79 */ instructionRA(this::instADC, this::opAbsoluteIndexedWithY),
        /* 0x7A */ instructionSi(this::instPLY),
        /* 0x7B */ instructionSi(this::instTDC),
        /* 0x7C */ instructionSi(this::instJMP, this::opAbsoluteIndexedIndirect),
        /* 0x7D */ instructionRA(this::instADC, this::opAbsoluteIndexedWithX),
        /* 0x7E */ instructionWA(this::instROR, this::opAbsoluteIndexedWithX),
        /* 0x7F */ instructionRA(this::instADC, this::opAbsoluteLongIndexedWithX),
        /* 0x80 */ instructionSi(this::instBRA, this::opProgramCounterRelative),
        /* 0x81 */ instructionSi(this::instSTA, this::opDirectIndexedIndirect),
        /* 0x82 */ instructionSi(this::instBRL, this::opProgramCounterRelativeLong),
        /* 0x83 */ instructionSi(this::instSTA, this::opStackRelative),
        /* 0x84 */ instructionSi(this::instSTY, this::opDirect),
        /* 0x85 */ instructionSi(this::instSTA, this::opDirect),
        /* 0x86 */ instructionSi(this::instSTX, this::opDirect),
        /* 0x87 */ instructionSi(this::instSTA, this::opDirectIndirectLong),
        /* 0x88 */ instructionSi(this::instDEY),
        /* 0x89 */ instructionSi(this::instBITImmediate, this::opImmediate, rA),
        /* 0x8A */ instructionSi(this::instTXA),
        /* 0x8B */ instructionSi(this::instPHB),
        /* 0x8C */ instructionSi(this::instSTY, this::opAbsolute),
        /* 0x8D */ instructionSi(this::instSTA, this::opAbsolute),
        /* 0x8E */ instructionSi(this::instSTX, this::opAbsolute),
        /* 0x8F */ instructionSi(this::instSTA, this::opAbsoluteLong),
        /* 0x90 */ instructionSi(this::instBCC, this::opProgramCounterRelative),
        /* 0x91 */ instructionSi(this::instSTA, this::opDirectIndirectIndexed),
        /* 0x92 */ instructionSi(this::instSTA, this::opDirectIndirect),
        /* 0x93 */ instructionSi(this::instSTA, this::opStackRelativeIndirectIndexed),
        /* 0x94 */ instructionSi(this::instSTY, this::opDirectIndexedWithX),
        /* 0x95 */ instructionSi(this::instSTA, this::opDirectIndexedWithX),
        /* 0x96 */ instructionSi(this::instSTX, this::opDirectIndexedWithY),
        /* 0x97 */ instructionSi(this::instSTA, this::opDirectIndirectLongIndexed),
        /* 0x98 */ instructionSi(this::instTYA),
        /* 0x99 */ instructionSi(this::instSTA, this::opAbsoluteIndexedWithY),
        /* 0x9A */ instructionSi(this::instTXS),
        /* 0x9B */ instructionSi(this::instTXY),
        /* 0x9C */ instructionSi(this::instSTZ, this::opAbsolute),
        /* 0x9D */ instructionSi(this::instSTA, this::opAbsoluteIndexedWithX),
        /* 0x9E */ instructionSi(this::instSTZ, this::opAbsoluteIndexedWithX),
        /* 0x9F */ instructionSi(this::instSTA, this::opAbsoluteLongIndexedWithX),
        /* 0xA0 */ instructionSi(this::instLDY, this::opImmediate, rY),
        /* 0xA1 */ instructionRA(this::instLDA, this::opDirectIndexedIndirect),
        /* 0xA2 */ instructionSi(this::instLDX, this::opImmediate, rX),
        /* 0xA3 */ instructionRA(this::instLDA, this::opStackRelative),
        /* 0xA4 */ instructionRY(this::instLDY, this::opDirect),
        /* 0xA5 */ instructionRA(this::instLDA, this::opDirect),
        /* 0xA6 */ instructionRX(this::instLDX, this::opDirect),
        /* 0xA7 */ instructionRA(this::instLDA, this::opDirectIndirectLong),
        /* 0xA8 */ instructionSi(this::instTAY),
        /* 0xA9 */ instructionSi(this::instLDA, this::opImmediate, rA),
        /* 0xAA */ instructionSi(this::instTAX),
        /* 0xAB */ instructionSi(this::instPLB),
        /* 0xAC */ instructionRY(this::instLDY, this::opAbsolute),
        /* 0xAD */ instructionRA(this::instLDA, this::opAbsolute),
        /* 0xAE */ instructionRX(this::instLDX, this::opAbsolute),
        /* 0xAF */ instructionRA(this::instLDA, this::opAbsoluteLong),
        /* 0xB0 */ instructionSi(this::instBCS, this::opProgramCounterRelative),
        /* 0xB1 */ instructionRA(this::instLDA, this::opDirectIndirectIndexed),
        /* 0xB2 */ instructionRA(this::instLDA, this::opDirectIndirect),
        /* 0xB3 */ instructionRA(this::instLDA, this::opStackRelativeIndirectIndexed),
        /* 0xB4 */ instructionRY(this::instLDY, this::opDirectIndexedWithX),
        /* 0xB5 */ instructionRA(this::instLDA, this::opDirectIndexedWithX),
        /* 0xB6 */ instructionRX(this::instLDX, this::opDirectIndexedWithY),
        /* 0xB7 */ instructionRA(this::instLDA, this::opDirectIndirectLongIndexed),
        /* 0xB8 */ instructionSi(this::instCLV),
        /* 0xB9 */ instructionRA(this::instLDA, this::opAbsoluteIndexedWithY),
        /* 0xBA */ instructionSi(this::instTSX),
        /* 0xBB */ instructionSi(this::instTYX),
        /* 0xBC */ instructionRY(this::instLDY, this::opAbsoluteIndexedWithX),
        /* 0xBD */ instructionRA(this::instLDA, this::opAbsoluteIndexedWithX),
        /* 0xBE */ instructionRX(this::instLDX, this::opAbsoluteIndexedWithY),
        /* 0xBF */ instructionRA(this::instLDA, this::opAbsoluteLongIndexedWithX),
        /* 0xC0 */ instructionSi(this::instCPY, this::opImmediate, rY),
        /* 0xC1 */ instructionRA(this::instCMP, this::opDirectIndexedIndirect),
        /* 0xC2 */ instructionSi(this::instREP, this::opImmediate, 1),
        /* 0xC3 */ instructionRA(this::instCMP, this::opStackRelative),
        /* 0xC4 */ instructionRY(this::instCPY, this::opDirect),
        /* 0xC5 */ instructionRA(this::instCMP, this::opDirect),
        /* 0xC6 */ instructionWA(this::instDEC, this::opDirect),
        /* 0xC7 */ instructionRA(this::instCMP, this::opDirectIndirectLong),
        /* 0xC8 */ instructionSi(this::instINY),
        /* 0xC9 */ instructionSi(this::instCMP, this::opImmediate, rA),
        /* 0xCA */ instructionSi(this::instDEX),
        /* 0xCB */ instructionSi(this::instWAI),
        /* 0xCC */ instructionRY(this::instCPY, this::opAbsolute),
        /* 0xCD */ instructionRA(this::instCMP, this::opAbsolute),
        /* 0xCE */ instructionWA(this::instDEC, this::opAbsolute),
        /* 0xCF */ instructionRA(this::instCMP, this::opAbsoluteLong),
        /* 0xD0 */ instructionSi(this::instBNE, this::opProgramCounterRelative),
        /* 0xD1 */ instructionRA(this::instCMP, this::opDirectIndirectIndexed),
        /* 0xD2 */ instructionRA(this::instCMP, this::opDirectIndirect),
        /* 0xD3 */ instructionRA(this::instCMP, this::opStackRelativeIndirectIndexed),
        /* 0xD4 */ instructionSi(this::instPEI, this::opDirect),
        /* 0xD5 */ instructionRA(this::instCMP, this::opDirectIndexedWithX),
        /* 0xD6 */ instructionWA(this::instDEC, this::opDirectIndexedWithX),
        /* 0xD7 */ instructionRA(this::instCMP, this::opDirectIndirectLongIndexed),
        /* 0xD8 */ instructionSi(this::instCLD),
        /* 0xD9 */ instructionRA(this::instCMP, this::opAbsoluteIndexedWithY),
        /* 0xDA */ instructionSi(this::instPHX),
        /* 0xDB */ instructionSi(this::instSTP),
        /* 0xDC */ instructionSi(this::instJML, this::opAbsoluteIndirect),
        /* 0xDD */ instructionRA(this::instCMP, this::opAbsoluteIndexedWithX),
        /* 0xDE */ instructionWA(this::instDEC, this::opAbsoluteIndexedWithX),
        /* 0xDF */ instructionRA(this::instCMP, this::opAbsoluteLongIndexedWithX),
        /* 0xE0 */ instructionSi(this::instCPX, this::opImmediate, rX),
        /* 0xE1 */ instructionRA(this::instSBC, this::opDirectIndexedWithX),
        /* 0xE2 */ instructionSi(this::instSEP, this::opImmediate, 1),
        /* 0xE3 */ instructionRA(this::instSBC, this::opStackRelative),
        /* 0xE4 */ instructionRX(this::instCPX, this::opDirect),
        /* 0xE5 */ instructionRA(this::instSBC, this::opDirect),
        /* 0xE6 */ instructionWA(this::instINC, this::opDirect),
        /* 0xE7 */ instructionRA(this::instSBC, this::opDirectIndirectLong),
        /* 0xE8 */ instructionSi(this::instINX),
        /* 0xE9 */ instructionSi(this::instSBC, this::opImmediate, rA),
        /* 0xEA */ instructionSi(this::instNOP),
        /* 0xEB */ instructionSi(this::instXBA),
        /* 0xEC */ instructionRX(this::instCPX, this::opAbsolute),
        /* 0xED */ instructionRA(this::instSBC, this::opAbsolute),
        /* 0xEE */ instructionWA(this::instINC, this::opAbsolute),
        /* 0xEF */ instructionRA(this::instSBC, this::opAbsoluteLong),
        /* 0xF0 */ instructionSi(this::instBEQ, this::opProgramCounterRelative),
        /* 0xF1 */ instructionRA(this::instSBC, this::opDirectIndirectIndexed),
        /* 0xF2 */ instructionRA(this::instSBC, this::opDirectIndirect),
        /* 0xF3 */ instructionRA(this::instSBC, this::opStackRelativeIndirectIndexed),
        /* 0xF4 */ instructionSi(this::instPEA, this::opImmediate, 2),
        /* 0xF5 */ instructionRA(this::instSBC, this::opDirectIndexedWithX),
        /* 0xF6 */ instructionWA(this::instINC, this::opDirectIndexedWithX),
        /* 0xF7 */ instructionRA(this::instSBC, this::opDirectIndirectLongIndexed),
        /* 0xF8 */ instructionSi(this::instSED),
        /* 0xF9 */ instructionRA(this::instSBC, this::opAbsoluteIndexedWithY),
        /* 0xFA */ instructionSi(this::instPLX),
        /* 0xFB */ instructionSi(this::instXCE),
        /* 0xFC */ instructionSi(this::instJSR, this::opAbsoluteIndexedIndirect),
        /* 0xFD */ instructionRA(this::instSBC, this::opAbsoluteIndexedWithX),
        /* 0xFE */ instructionWA(this::instINC, this::opAbsoluteIndexedWithX),
        /* 0xFF */ instructionRA(this::instSBC, this::opAbsoluteLongIndexedWithX)
    )

    /** simple instruction, no operand, no return, no read, no write */
    private inline fun instructionSi(crossinline action: () -> Any)
            = Instruction { action() }
    /** simple instruction with one operand, no return, no read, no write */
    private inline fun <O> instructionSi(crossinline action: (O) -> Unit, crossinline operand: () -> O)
            = Instruction { action(operand()) }
    /** instruction with read from address, the address is given by the operand and the read-size is determined by the given register */
    private inline fun instructionRR(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { action(readFor(register, getAddress())) }
    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by the given register */
    private inline fun instructionWR(crossinline  action: (Int) -> Int, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { getAddress().let { writeFor(register, it, action(readFor(register, it))) } }
    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by the given register */
    private inline fun instructionWR(crossinline  action: (DiffSizeRegister, Int) -> Int, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { getAddress().let { writeFor(register, it, action(register, readFor(register, it))) } }
    /** instruction which reads the operand from A and sets A */
    private inline fun instructionSR(crossinline action: (Int) -> Int, register: Register)
            = Instruction { register.set(action(register.get())) }
    /** instruction which reads the operand from A and sets A */
    private inline fun <R : Register> instructionSR(crossinline action: (R, Int) -> Int, register: R)
            = Instruction { register.set(action(register, register.get())) }


    /** simple instruction with one operand, no return, no read, no write, this is only used for immediate operand to define how many bytes to read */
    private inline fun <O> instructionSi(crossinline action: (O) -> Unit, crossinline operand: (Int) -> O, register: DiffSizeRegister)
            = instructionSi(action, operand, if (register._8bitMode) 1 else 2)
    /** simple instruction with one operand, no return, no read, no write , this is only used for immediate operand to define how many bytes to read*/
    private inline fun <O> instructionSi(crossinline action: (O) -> Unit, crossinline operand: (Int) -> O, arg: Int)
            = Instruction { action(operand(arg)) }

    /** instruction with read from address, the address is given by the operand and the read-size is determined by A */
    private inline fun instructionRA(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, rA)

    /** instruction with read from address, the address is given by the operand and the read-size is determined by X */
    private inline fun instructionRX(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, rX)

    /** instruction with read from address, the address is given by the operand and the read-size is determined by Y */
    private inline fun instructionRY(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, rY)

    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by A */
    private inline fun instructionWA(crossinline  action: (Int) -> Int, crossinline getAddress: () -> FullAddress) = instructionWR(action, getAddress, rA)
    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by A */
    private inline fun instructionWA(crossinline  action: (DiffSizeRegister, Int) -> Int, crossinline getAddress: () -> FullAddress) = instructionWR(action, getAddress, rA)

    /** instruction which reads the operand from A and sets A */
    private inline fun instructionSA(crossinline action: (Int) -> Int)
            = instructionSR(action, rA)
    /** instruction which reads the operand from A and sets A */
    private inline fun instructionSA(crossinline action: (DiffSizeRegister, Int) -> Int)
            = instructionSR(action, rA)

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

        rPC.value = readWord(
            BankNo(rDBR.value),
            RESET_VECTOR_ADDRESS
        )
    }

    private fun fetch(): Int {
        val v = readByte(
            BankNo(rPBR.value),
            ShortAddress(rPC.value)
        )
        rPC.inc()
        return v
    }

    private fun fetchShort()
        = fetch() + (fetch() shl 8)
    private fun fetchLongAddress()
        = fetchShort() + (fetch() shl 16)

    /** a */
    private fun opAbsolute() = FullAddress(
        BankNo(rDBR.value),
        ShortAddress(fetchShort())
    )
    /** (a, x) */
    private fun opAbsoluteIndexedIndirect() = FullAddress(
        BankNo(rPBR.value),
        shortAddress(fetchShort() + rX)
    )
    /** a,x */
    private fun opAbsoluteIndexedWithX() = FullAddress(
        BankNo(rDBR.value),
        shortAddress(fetchShort() + rX)
    )
    /** a,y */
    private fun opAbsoluteIndexedWithY() = FullAddress(
        BankNo(rDBR.value),
        shortAddress(fetchShort() + rY.value)
    )
    /** (a) */
    private fun opAbsoluteIndirect() = FullAddress(
        BankNo(0),
        ShortAddress(fetchShort())
    )
    /** al,x */
    private fun opAbsoluteLongIndexedWithX() =
        fullAddress(fetchLongAddress() + rX)
    /** al */
    private fun opAbsoluteLong() = FullAddress(fetchLongAddress())
    /** (d,x) */
    private fun opDirectIndexedIndirect() = FullAddress(
        BankNo(rDBR.value),
        ShortAddress(
            readWord(
                BankNo(
                    0
                ), shortAddress(fetch() + rD + rX)
            )
        )
    )
    /** d,x */
    private fun opDirectIndexedWithX() = FullAddress(
        BankNo(0),
        shortAddress(fetch() + rD + rX)
    )
    /** d.y */
    private fun opDirectIndexedWithY() = FullAddress(
        BankNo(0),
        shortAddress(fetch() + rD + rY)
    )
    /** (d),y */
    private fun opDirectIndirectIndexed() = FullAddress(
        BankNo(rDBR.value),
        shortAddress(
            readWord(
                BankNo(
                    0
                ), shortAddress(fetchShort() + rD)
            ) + rY
        )
    )
    /** \[d],y */
    private fun opDirectIndirectLongIndexed() = fullAddress(
        readLong(
            BankNo(0),
            shortAddress(fetch() + rD)
        ) + rY
    )
    /** \[d] */
    private fun opDirectIndirectLong() = FullAddress(
        readLong(
            BankNo(0),
            ShortAddress(fetch() + rD)
        )
    )
    /** (d) */
    private fun opDirectIndirect() = FullAddress(
        BankNo(rDBR.value),
        ShortAddress(
            readWord(
                BankNo(
                    0
                ), shortAddress(fetch() + rD)
            )
        )
    )
    /** d */
    private fun opDirect() = FullAddress(
        BankNo(0),
        shortAddress(fetch() + rD)
    )
    /** # */
    private fun opImmediate(cnt: Int) = if (cnt == 1) fetch() else fetchShort()
    /** rl */
    // toShort converts it to signed, and toInt is needed for the calculation
    private fun opProgramCounterRelativeLong() =
        shortAddress(fetchShort().toShort().toInt() + rPC)
    /** r */
    // toByte converts it to signed, and toInt is needed for the calculation
    private fun opProgramCounterRelative() =
        shortAddress(fetch().toByte().toInt() + rPC)
    /** d,s */
    private fun opStackRelative() = FullAddress(
        BankNo(0),
        shortAddress(fetch() + rS)
    )
    /** (d,s),y */
    private fun opStackRelativeIndirectIndexed() = FullAddress(
        BankNo(rDBR.value),
        shortAddress(
            readWord(
                BankNo(
                    0
                ), shortAddress(fetch() + rS)
            ) + rY
        )
    )
    // Accumulator -A
    // Block move -xyc
    // implied -i -> no further bytes used -> operand defined by instruction
    // stack -s

    /** break interrupt */
    private fun instBRK() {
        interrupt(if (mode == ProcessorMode.EMULATION) EMULATION_BRK_VECTOR_ADDRESS else NATIVE_BRK_VECTOR_ADDRESS)
    }

    /** coprocessor interrupt */
    private fun instCOP() {
        interrupt(COP_VECTOR_ADDRESS)
    }

    private fun interrupt(interruptAddress: ShortAddress) {
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

        rPC.value = readWord(BankNo(0), interruptAddress)
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
    private fun instBPL(address: ShortAddress) {
        branchIf(address) {!rP.negative }
    }

    /** Branch if minus - branches if negative is set */
    private fun instBMI(address: ShortAddress) {
        branchIf(address) { rP.negative }
    }

    /** Branch if overflow set */
    private fun instBVS(address: ShortAddress) {

        branchIf(address) { rP.overflow }
    }

    /** branch if overflow clear */
    private fun instBVC(address: ShortAddress) {
        branchIf(address) { !rP.overflow }
    }

    /** Branch if carry set */
    private fun instBCS(address: ShortAddress) {
        branchIf(address) { rP.carry }
    }

    /** Branch if carry clear */
    private fun instBCC(address: ShortAddress) {
        branchIf(address) {!rP.carry }
    }

    /** Branch if zero set/equal */
    private fun instBEQ(address: ShortAddress) {
        branchIf(address) { rP.zero }
    }

    /** Branch if Zero clear/not Equal */
    private fun instBNE(address: ShortAddress) {
        branchIf(address) { !rP.zero }
    }

    private fun branchIf(address: ShortAddress, check: () -> Boolean) {
        if (check()) {
            branch(address)
        }
    }

    /** Branch always */
    private fun instBRA(address: ShortAddress) {
        branch(address)
    }

    /** Branch always long */
    private fun instBRL(address: ShortAddress) {
        branch(address)
    }

    /** Jump to subroutine (given address without bank) - pushes the current PC minus 1 */
    private fun instJSR(address: FullAddress) {
        jump(address.shortAaddress)
    }

    /** Jump to subroutine (given address including bank), pushes the current PC minus 1 and the PBR */
    private fun instJSL(address: FullAddress) {
        jump(address)
    }

    /** Jump to full address including bank */
    private fun instJMP(address: FullAddress) {
        branch(address)
    }

    /** Jump Long - jump to the given address bank included */
    private fun instJML(address: FullAddress) {
        branch(address)
    }

    private fun jump(address: ShortAddress) {
        rS.pushShort(rPC.value - 1)
        branch(address)
    }

    private fun branch(address: ShortAddress) {
        rPC.value = address.shortAddress
    }

    private fun jump(address: FullAddress) {
        rS.pushByte(rPBR.value)
        rS.pushShort(rPC.value - 1)
        jump(address)
    }

    private fun branch(address: FullAddress) {
        branch(address.shortAaddress)
        rPBR.value = address.bankNo.bankNo
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
        rS.pushShort(address.shortAaddress.shortAddress)
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
    private fun instPER() {
        rS.pushShort(fetchShort() + rPC.value)
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
        val bankSource = BankNo(fetch())
        val bankDest = BankNo(fetch())

        while (!rA.zero) {
            writeByte(bankDest, ShortAddress(rY.get()),
                readByte(bankSource, ShortAddress(rX.get())))

            rA.dec()
            rX.dec()
            rY.dec()
        }

        writeByte(bankDest, ShortAddress(rY.get()),
            readByte(bankSource, ShortAddress(rX.get())))

        rA.dec()
        rX.dec()
        rY.dec()
    }

    /** Block move negative */
    private fun instMVN() {
        val bankSource = BankNo(fetch())
        val bankDest = BankNo(fetch())

        while (!rA.zero) {
            writeByte(bankDest, ShortAddress(rY.get()),
                readByte(bankSource, ShortAddress(rX.get())))

            rA.dec()
            rX.inc()
            rY.inc()
        }

        writeByte(bankDest, ShortAddress(rY.get()),
            readByte(bankSource, ShortAddress(rX.get())))

        rA.dec()
        rX.inc()
        rY.inc()
    }

    /** Wait for interrupt */
    private fun instWAI() {
        // TODO
        error("not implemented yet")
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

    private fun readByte(bank: BankNo, address: ShortAddress): Int {
        return readByte(bank, address)
    }

    private fun readWord(bank: BankNo, address: ShortAddress)
        = readByte(bank, address) + (readByte(bank, shortAddress(address.shortAddress + 1)) shl 8)

    private fun readLong(bank: BankNo, address: ShortAddress)
        = readWord(bank, address) + (readByte(bank, shortAddress(address.shortAddress + 2)) shl 16)

    private fun writeByte(bank: BankNo, address: ShortAddress, value: Int) {
        writeByte(bank, address, value)
    }

    private fun readFor(register: DiffSizeRegister, address: FullAddress)
    = if(register._8bitMode) readByte(address.bankNo, address.shortAaddress) else readWord(address.bankNo, address.shortAaddress)

    private fun writeWord(bank: BankNo, address: ShortAddress, value: Int) {
        writeByte(bank, address, value)
        writeByte(bank, shortAddress(address.shortAddress + 1), value shr 8)
    }

    private fun writeLong(bank: BankNo, address: ShortAddress, value: Int) {
        writeWord(bank, address, value)
        writeByte(bank, shortAddress(address.shortAddress + 2), value shr 16)
    }

    private fun writeFor(register: DiffSizeRegister, address: FullAddress, value: Int = register.value)
            = if (register._8bitMode) writeByte(address.bankNo, address.shortAaddress, value) else writeWord(address.bankNo, address.shortAaddress, value)

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
            irqDisable = false
            decimal = false
            index = false
            memory = false
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

        fun pushByte(value: Int) {
            writeByte(
                BankNo(0),
                ShortAddress(this.value), value)
            dec()
        }

        fun pushShort(value: Int) {
            pushByte(value shr 8)
            pushByte(value)
        }

        fun pullByte(): Int {
            inc()
            return readByte(
                BankNo(0),
                ShortAddress(value)
            )
        }

        fun pullShort(): Int {
            return pullByte() + (pullByte() shl 8)
        }
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

        private val COP_VECTOR_ADDRESS: ShortAddress =
            ShortAddress(0xFFF4)
        private val NATIVE_BRK_VECTOR_ADDRESS: ShortAddress =
            ShortAddress(0xFFF6)
        private val ABORT_VECTOR_ADDRESS: ShortAddress =
            ShortAddress(0xFFF8)
        private val NMI_VECTOR_ADDRESS: ShortAddress =
            ShortAddress(0xFFFA)
        private val RESET_VECTOR_ADDRESS: ShortAddress =
            ShortAddress(0xFFFC)
        private val IRQ_VECTOR_ADDRESS: ShortAddress =
            ShortAddress(0xFFFE)
        private val EMULATION_BRK_VECTOR_ADDRESS: ShortAddress = IRQ_VECTOR_ADDRESS

        private operator fun Int.plus(s: StackPointer) = if(s._8bitMode) this + s.value and 0xFF else this + s.value
        private operator fun Int.plus(a: Accumulator) = if (a._8bitMode) this + a.value and 0xFF else this + a.value
        private operator fun Int.plus(r: Register) = this + r.value
    }
}
