package de.dde.snes.processor

import de.dde.snes.*


class Processor(
    val memory: Memory
) {
    var mode = ProcessorMode.EMULATION

    // registers
    var A = Accumulator() // accumulator
    var DBR = Register8Bit() // Data Bank Register
    var D = Register16Bit() // Direct
    var X = IndexRegister() // X Index Register
    var Y = IndexRegister() // Y Index Register
    var P = StatusRegister() // Processor Status Register
    var PBR = Register8Bit() // Program Bank Register
    var PC = Register16Bit() // Program Counter
    var S = StackPointer() // Stack Pointer

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
        /* 0x09 */ instructionSi(this::instORA, this::opImmediate, A),
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
        /* 0x29 */ instructionSi(this::instAND, this::opImmediate, A),
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
        /* 0x49 */ instructionSi(this::instEOR, this::opImmediate, A),
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
        /* 0x69 */ instructionSi(this::instADC, this::opImmediate, A),
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
        /* 0x89 */ instructionSi(this::instBIT, this::opImmediate, A),
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
        /* 0xA0 */ instructionSi(this::instLDY, this::opImmediate, Y),
        /* 0xA1 */ instructionRA(this::instLDA, this::opDirectIndexedIndirect),
        /* 0xA2 */ instructionSi(this::instLDX, this::opImmediate, X),
        /* 0xA3 */ instructionRA(this::instLDA, this::opStackRelative),
        /* 0xA4 */ instructionRY(this::instLDY, this::opDirect),
        /* 0xA5 */ instructionRA(this::instLDA, this::opDirect),
        /* 0xA6 */ instructionRX(this::instLDX, this::opDirect),
        /* 0xA7 */ instructionRA(this::instLDA, this::opDirectIndirectLong),
        /* 0xA8 */ instructionSi(this::instTAY),
        /* 0xA9 */ instructionSi(this::instLDA, this::opImmediate, A),
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
        /* 0xC0 */ instructionSi(this::instCPY, this::opImmediate, Y),
        /* 0xC1 */ instructionRA(this::instCMP, this::opDirectIndexedIndirect),
        /* 0xC2 */ instructionSi(this::instREP, this::opImmediate, 1),
        /* 0xC3 */ instructionRA(this::instCMP, this::opStackRelative),
        /* 0xC4 */ instructionRY(this::instCPY, this::opDirect),
        /* 0xC5 */ instructionRA(this::instCMP, this::opDirect),
        /* 0xC6 */ instructionWA(this::instDEC, this::opDirect),
        /* 0xC7 */ instructionRA(this::instCMP, this::opDirectIndirectLong),
        /* 0xC8 */ instructionSi(this::instINY),
        /* 0xC9 */ instructionSi(this::instCMP, this::opImmediate, A),
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
        /* 0xE0 */ instructionSi(this::instCPX, this::opImmediate, X),
        /* 0xE1 */ instructionRA(this::instSBC, this::opDirectIndexedWithX),
        /* 0xE2 */ instructionSi(this::instSEP, this::opImmediate, 1),
        /* 0xE3 */ instructionRA(this::instSBC, this::opStackRelative),
        /* 0xE4 */ instructionRX(this::instCPX, this::opDirect),
        /* 0xE5 */ instructionRA(this::instSBC, this::opDirect),
        /* 0xE6 */ instructionWA(this::instINC, this::opDirect),
        /* 0xE7 */ instructionRA(this::instSBC, this::opDirectIndirectLong),
        /* 0xE8 */ instructionSi(this::instINX),
        /* 0xE9 */ instructionSi(this::instSBC, this::opImmediate, A),
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
    private inline fun instructionSi(crossinline action: (Int) -> Unit, crossinline operand: () -> Int)
            = Instruction { action(operand()) }
    /** simple instruction with one operand, no return, no read, no write */
    private inline fun instructionSi(crossinline action: (Int) -> Unit, crossinline operand: (Int) -> Int, register: DiffSizeRegister)
            = instructionSi(action, operand, if (register._8bitMode) 1 else 2)
    /** simple instruction with one operand, no return, no read, no write */
    private inline fun instructionSi(crossinline action: (Int) -> Unit, crossinline operand: (Int) -> Int, arg: Int)
            = Instruction { action(operand(arg)) }

    /** instruction with read from address, the address is given by the operand and the read-size is determined by A */
    private inline fun instructionRA(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, A)
    /** instruction with read from address, the address is given by the operand and the read-size is determined by A */
    private inline fun instructionRA(crossinline action: (Int) -> Unit, crossinline getAddress: (DiffSizeRegister?) -> FullAddress) = instructionRR(action, getAddress, A)
    /** instruction with read from address, the address is given by the operand and the read-size is determined by A */
    private inline fun instructionRA(crossinline action: (DiffSizeRegister, Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, A)

    /** instruction with read from address, the address is given by the operand and the read-size is determined by X */
    private inline fun instructionRX(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, X)
    /** instruction with read from address, the address is given by the operand and the read-size is determined by X */
    private inline fun instructionRX(crossinline action: (DiffSizeRegister, Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, X)

    /** instruction with read from address, the address is given by the operand and the read-size is determined by Y */
    private inline fun instructionRY(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, Y)
    /** instruction with read from address, the address is given by the operand and the read-size is determined by Y */
    private inline fun instructionRY(crossinline action: (DiffSizeRegister, Int) -> Unit, crossinline getAddress: () -> FullAddress) = instructionRR(action, getAddress, Y)

    /** instruction with read from address, the address is given by the operand and the read-size is determined by the given register */
    private inline fun instructionRR(crossinline action: (Int) -> Unit, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { action(memory.readFor(register, getAddress())) }
    /** instruction with read from address, the address is given by the operand and the read-size is determined by the given register */
    private inline fun instructionRR(crossinline action: (Int) -> Unit, crossinline getAddress: (DiffSizeRegister?) -> FullAddress, register: DiffSizeRegister)
            = Instruction { action(memory.readFor(register, getAddress(register))) }
    /** instruction with read from address, the address is given by the operand and the read-size is determined by the given register */
    private inline fun instructionRR(crossinline action: (DiffSizeRegister, Int) -> Unit, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { action(register, memory.readFor(register, getAddress())) }

    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by A */
    private inline fun instructionWA(crossinline  action: (Int) -> Int, crossinline getAddress: () -> FullAddress) = instructionWR(action, getAddress, A)
    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by A */
    private inline fun instructionWA(crossinline  action: (DiffSizeRegister, Int) -> Int, crossinline getAddress: () -> FullAddress) = instructionWR(action, getAddress, A)

    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by the given register */
    private inline fun instructionWR(crossinline  action: (Int) -> Int, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { getAddress().let { memory.writeFor(register, it, action(memory.readFor(register, it))) } }
    /** instruction with read from and write to address, the address is given by the operand and the read/write-size is determined by the given register */
    private inline fun instructionWR(crossinline  action: (DiffSizeRegister, Int) -> Int, crossinline getAddress: () -> FullAddress, register: DiffSizeRegister)
            = Instruction { getAddress().let { memory.writeFor(register, it, action(register, memory.readFor(register, it))) } }

    /** instruction which reads the operand from A and sets A */
    private inline fun instructionSA(crossinline action: (Int) -> Int)
            = Instruction { A.set(action(A.get())) }
    /** instruction which reads the operand from A and sets A */
    private inline fun instructionSA(crossinline action: (DiffSizeRegister, Int) -> Int)
            = Instruction { A.set(action(A, A.get())) }

    fun reset() {
        mode = ProcessorMode.EMULATION
        P.reset()

        A.reset()
        DBR.reset()
        D.reset()
        X.reset()
        Y.reset()
        PBR.reset()
        S.reset()

        PC.value = memory.readShort(
            DBR.value,
            RESET_VECTOR_ADDRESS
        )
    }

    private fun fetch(): Int {
        val v = memory.readByte(PBR.value, PC.value)
        PC.inc()
        return v
    }

    private inline fun fetchShort(): ShortAddress = fetch() + (fetch() shl 8)
    private inline fun fetchLongAddress(): FullAddress = fetchShort() + (fetch() shl 16)

    /** a */
    private fun opAbsolute() = FullAddress(DBR.value, fetchShort())
    /** (a, x) */
    private fun opAbsoluteIndexedIndirect() = FullAddress(PBR.value, ShortAddress(fetchShort() + X))
    /** a,x */
    private fun opAbsoluteIndexedWithX() = FullAddress(DBR.value, ShortAddress(fetchShort() + X))
    /** a,y */
    private fun opAbsoluteIndexedWithY() = FullAddress(DBR.value, ShortAddress(fetchShort() + Y.value))
    /** (a) */
    private fun opAbsoluteIndirect() = FullAddress(0, fetchShort())
    /** al,x */
    private fun opAbsoluteLongIndexedWithX(): FullAddress = fetchLongAddress() + X
    /** al */
    private fun opAbsoluteLong() = fetchLongAddress()
    /** (d,x) */
    private fun opDirectIndexedIndirect() = FullAddress(DBR.value, ShortAddress(memory.readShort(0, ShortAddress(fetch() + D + X))))
    /** d,x */
    private fun opDirectIndexedWithX() = FullAddress(0, ShortAddress(fetch() + D + X))
    /** d.y */
    private fun opDirectIndexedWithY() = FullAddress(0, ShortAddress(fetch() + D + Y))
    /** (d),y */
    private fun opDirectIndirectIndexed() = FullAddress(DBR.value, ShortAddress(memory.readShort(0, ShortAddress(fetchShort() + D)) + Y))
    /** \[d],y */
    private fun opDirectIndirectLongIndexed(): FullAddress = memory.readAddress(0, ShortAddress(fetch() + D)) + Y
    /** \[d] */
    private fun opDirectIndirectLong(): FullAddress = memory.readAddress(0, ShortAddress(fetch() + D))
    /** (d) */
    private fun opDirectIndirect() = FullAddress(DBR.value, memory.readShort(0, ShortAddress(fetch() + D)))
    /** d */
    private fun opDirect() = FullAddress(0, ShortAddress(fetch() + D))
    /** # */
    private fun opImmediate(cnt: Int) = if (cnt == 1) fetch() else fetchShort()
    /** rl */
    // toShort converts it to signed, and toInt is needed for the calculation
    private fun opProgramCounterRelativeLong() = ShortAddress(fetchShort().toShort().toInt() + PC)
    /** r */
    // toByte converts it to signed, and toInt is needed for the calculation
    private fun opProgramCounterRelative() = ShortAddress(fetch().toByte().toInt() + PC)
    /** d,s */
    private fun opStackRelative() = FullAddress(0, ShortAddress(fetch() + S))
    /** (d,s),y */
    private fun opStackRelativeIndirectIndexed() = FullAddress(DBR.value, memory.readShort(0, ShortAddress(fetch() + S)) + Y)
    // Accumulator -A
    // Block move -xyc
    // implied -i -> no further bytes used -> operand defined by instruction
    // stack -s

    /** break interrupt */
    private fun instBRK() {
        // TODO interrupt
        error("not implemented yet")
    }

    /** coprocessor interrupt */
    private fun instCOP() {
        // TODO interrupt
        error("snes doesn't have any coprocessors")
    }

    /** Test and Set Bit */
    private fun instTSB(value: Int): Int {
        P.zero = A and value == 0
        return A or value
    }

    /** Test and Reset Bit */
    private fun instTRB(value: Int): Int {
        P.zero = A and value == 0
        return A.valueOf(value and A.inv)
    }

    /** Bit-test between A and the given value - no value is changed, only flags are set */
    private fun instBIT(value: Int) {
        val res = A and value
        P.zero = res == 0
        P.overflow = A.overflow(value)
        P.negative = A.negative(value)
    }

    /** Clear carry flag */
    private fun instCLC() {
        P.carry = false
    }

    /** Set carry */
    private fun instSEC() {
        P.carry = true
    }

    /** clear irq/interrupt flag */
    private fun instCLI() {
        P.irqDisable = false
    }

    /** Set irq/interrupt flag */
    private fun instSEI() {
        P.irqDisable = true
    }

    /** Clear overflow flag */
    private fun instCLV() {
        P.overflow = false
    }

    /** Clear decimal flag */
    private fun instCLD() {
        P.decimal = false
    }

    /** Set Bit in P */
    private fun instSEP(value: Int) {
        P.fromInt(P.asInt() or value)
        A.checkBitMode()
        X.checkBitMode()
        Y.checkBitMode()
    }

    /** Set decimal flag */
    private fun instSED() {
        P.decimal = true
    }

    /** Reset Bit in P */
    private fun instREP(value: Int) {
        // reset any bit of P set in value
        var p = P.asInt()
        p = p and value.inv()
        P.fromInt(p)
        A.checkBitMode()
        X.checkBitMode()
        Y.checkBitMode()
    }

    /** Exchange Carry and Emulation-flag */
    private fun instXCE() {
        val t = P.carry
        P.carry = mode == ProcessorMode.EMULATION
        mode = if (t) ProcessorMode.EMULATION else ProcessorMode.NATIVE
        P.fromInt(P.asInt())

        A.checkBitMode()
        X.checkBitMode()
        Y.checkBitMode()
    }

    /** Compare given value with Y */
    private fun instCPY(value: Int) {
        val v = Y.get() - Y.valueOf(value)
        P.carry = v >= 0
        P.zero = v == 0
        P.negative = Y.negative(v)
    }

    /** Compare given value with A */
    private fun instCMP(value: Int) {
        val v = A.get() - A.valueOf(value)
        P.carry = v >= 0
        P.zero = v == 0
        P.negative = A.negative(v)
    }

    /** Compare the given value with X */
    private fun instCPX(value: Int) {
        val v = X.get() - X.valueOf(value)
        P.carry = v >= 0
        P.zero = v == 0
        P.negative = X.negative(v)
    }

    /** branch if plus - branches if negative is not set */
    private fun instBPL(address: ShortAddress) {
        if(!P.negative) {
            PC.value = address
        }
    }

    /** Branch if minus - branches if negative is set */
    private fun instBMI(address: ShortAddress) {
        if(P.negative) {
            PC.value = address
        }
    }

    /** branch if overflow clear */
    private fun instBVC(address: FullAddress) {
        if(!P.overflow) {
            PC.value = address.address
        }
    }

    /** Branch if overflow set */
    private fun instBVS(address: FullAddress) {
        if (P.overflow) {
            PC.value = address.address
        }
    }

    /** Branch always */
    private fun instBRA(address: FullAddress) {
        PC.value = address.address
    }

    /** Branch always long */
    private fun instBRL(address: FullAddress) {
        PC.value = address.address
    }

    /** Branch if carry clear */
    private fun instBCC(address: FullAddress) {
        if (!P.carry) {
            PC.value = address.address
        }
    }

    /** Branch if carry set */
    private fun instBCS(address: FullAddress) {
        if (P.carry) {
            PC.value = address.address
        }
    }

    /** Branch if Zero clear/not Equal */
    private fun instBNE(address: FullAddress) {
        if(!P.zero) {
            PC.value = address.address
        }
    }

    /** Branch if zero set/equal */
    private fun instBEQ(address: FullAddress) {
        if (P.zero) {
            PC.value = address.address
        }
    }

    /** perform or with accumulator */
    private fun instORA(value: Int) {
        A.set(A or value)
        P.zero = A.zero
        P.negative = A.negative
    }

    /** perform and with accumulator */
    private fun instAND(value: Int) {
        A.set(A and value)
        P.zero = A.zero
        P.negative = A.negative
    }

    /** perform exlusive or with A */
    private fun instEOR(value: Int) {
        A.set(A xor value)
        P.zero = A.zero
        P.negative = A.negative
    }

    /** shift left */
    private fun instASL(value: Int): Int {
        P.carry = A.negative(value)
        val v = A.valueOf(value shl 1)
        P.zero = v == 0
        P.negative = A.negative(v)
        return v
    }

    /** shift right without carry */
    private fun instLSR(value: Int): Int {
        P.carry = value and 1 != 0
        val v = value shr 1
        P.zero = v == 0
        P.negative = A.negative(v)
        return v
    }

    /** shift left with carry */
    private fun instROL(value: Int): Int {
        var v = value shl 1
        if (P.carry) v++

        val vv = A.valueOf(v)

        // v is shifted and has one bit more than vv if and overflow occured
        P.carry = v != vv
        P.zero = vv != 0
        P.negative = A.negative(vv)
        return vv
    }

    /** shift right with carry */
    private fun instROR(register: DiffSizeRegister, value: Int): Int {
        var res = value
        if (P.carry) {
            res += if (register._8bitMode) 0x100 else 0x10000
        }
        res = res shr 1
        // if carry is set, the left-most bit is added after rotating to right, this is effectively the negative-bit
        P.negative = P.carry
        P.carry = value and 0x1 != 0
        P.zero = res == 0
        return res
    }

    /** Increment */
    private fun instINC(value: Int): Int {
        val v = value + 1
        P.zero = A.zero(v)
        P.negative = A.negative(v)
        return v
    }

    /** Decrement */
    private fun instDEC(value: Int): Int {
        return value - 1
    }

    /** Decrement Y */
    private fun instDEY() {
        Y.dec()
        P.zero = Y.zero
        P.negative = Y.negative
    }

    /** Increment Y */
    private fun instINY() {
        Y.inc()
        P.zero = Y.zero
        P.negative = Y.negative
    }

    /** Decrement X */
    private fun instDEX() {
        X.dec()
        P.zero = X.zero
        P.negative = X.negative
    }

    /** Increment X */
    private fun instINX() {
        X.inc()
        P.zero = X.zero
        P.negative = X.negative
    }

    /** add with carry */
    private fun instADC(value: Int) {
        // TODO make pretty, currently its just copied from snes9x
        var v = if (P.decimal) {
            var res = (A.value and 0xF) + (value and 0xF) + (if (P.carry) 0x1 else 0)
            if (res > 0x9) res += 0x6
            P.carry = res > 0xF
            res = (A.value and 0xF0) + (value and 0xF0) + (if (P.carry) 0x10 else 0) + (res and 0xF)
            if (!A._8bitMode) {
                if (res > 0x9F) res += 0x60
                P.carry = res > 0xFF
                res = (A.value and 0xF00) + (value and 0xF00) + (if (P.carry) 0x100 else 0) + (res and 0xFF)
                if (res > 0x9FF) res += 0x600
                P.carry = res > 0xFFF
                res = (A.value and 0xF000) + (value and 0xF000) + (if (P.carry) 0x1000 else 0) + (res and 0xFFF)
            }
            res
        } else {
            A.get() + value + if (P.carry) 1 else 0
        }

        // (A.value xor value).inv() the sign-bit is 1 only if both A and value have the same sign
        // (A.value xor v) the sign-bit is set, if A (the start) and v (the end) have different signs
        // so if these two are combined using and, a set sign-bit shows, that A overflowed
        P.overflow = A.negative((A.value xor value).inv() and (A.value xor v))

        if (P.decimal) {
            if (A._8bitMode && v > 0x9F) v += 0x60
            else if (!A._8bitMode && v > 0x9FFF) v += 0x6000
        }

        A.set(v)

        P.negative = A.negative
        P.zero = A.zero
        P.carry = A.get() != v
    }

    /** Subtract with carry */
    private fun instSBC(value: Int) {
        // TODO make pretty, currently its just copied from snes9x

        // if carry is set, the calculation is as expected (i. e. 0x9 - 0x6 = 0x3)
        // without carry, we have to subtract one more (i. e. 0x9 - 0x6 = 0x2)

        val valueInv = value.inv()
        var v = if (P.decimal) {
            var result = (A.value and 0xf) + (valueInv and 0xf) + (if (P.carry) 0x1 else 0)
            if(result <= 0xf) result -= 0x6;
            P.carry = result > 0x000f;
            result = (A.value and 0xf0) + (valueInv and 0xf0) + (if (P.carry) 0x10 else 0) + (result and 0x000f)

            if (!A._8bitMode) {
                if (result <= 0xff) result -= 0x60;
                P.carry = result > 0xff;
                result = (A.value and  0xf00) + (valueInv and 0xf00) + (if (P.carry) 0x100 else 0) + (result and 0x00ff)
                if (result <= 0xfff) result -= 0x600;
                P.carry = result > 0xfff;
                result = (A.value and 0xf000) + (valueInv and 0xf000) + (if (P.carry) 0x1000 else 0) + (result and 0x0fff);
            }
            result
        } else {
            A.get() + A.valueOf(valueInv) + (if (P.carry) 1 else 0)
        }

        P.overflow = A.negative((A.value xor valueInv).inv() and (A.value xor v))
        if (P.decimal) {
            if (A._8bitMode && v <= 0xff) v -= 0x60 else if (!A._8bitMode && v <= 0xFFFF) v -= 0x6000
        }
        A.set(v)
        P.carry = v != A.get()
        P.zero = A.zero
        P.negative = A.negative
    }

    /** push P */
    private fun instPHP() {
        S.pushByte(P.asInt())
    }

    /** push D */
    private fun instPHD() {
        S.pushShort(D.value)
    }

    /** push A */
    private fun instPHA() {
        if (A._8bitMode) {
            S.pushByte(A.value)
        } else {
            S.pushShort(A.value)
        }
    }

    /** push PBR */
    private fun instPHK() {
        S.pushByte(PBR.value)
    }

    /** push Y */
    private fun instPHY() {
        if (Y._8bitMode) {
            S.pushByte(Y.value)
        } else {
            S.pushShort(Y.value)
        }
    }

    /** Push effective indirect address - pushes the given address */
    private fun instPEI(address: FullAddress) {
        S.pushShort(address.address)
    }

    /** push X */
    private fun instPHX() {
        if (X._8bitMode) {
            S.pushByte(X.value)
        } else {
            S.pushShort(X.value)
        }
    }

    /** Push effective address */
    private fun instPEA(address: FullAddress) {
        S.pushShort(address.address)
    }

    /** Pull X */
    private fun instPLX() {
        X.set(if (X._8bitMode) S.pullByte() else S.pullShort())
        P.zero = X.zero
        P.negative = X.negative
    }

    /** Pull P */
    private fun instPLP() {
        P.fromInt(S.pullByte())

        A.checkBitMode()
        X.checkBitMode()
        Y.checkBitMode()
    }

    /** Pull D */
    private fun instPLD() {
        D.value = S.pullShort()
        P.zero = D.value == 0
        P.negative = D.negative
    }

    /** Pull A */
    private fun instPLA() {
        A.set(if (A._8bitMode) S.pullByte() else S.pullShort())
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Pull Y */
    private fun instPLY() {
        Y.set(if (Y._8bitMode) S.pullByte() else S.pullShort())
        P.zero = Y.zero
        P.negative = Y.negative
    }

    /** Push DBR */
    private fun instPHB() {
        S.pushByte(DBR.value)
    }


    /** Pull DBR */
    private fun instPLB() {
        DBR.value = S.pullByte()
        P.zero = DBR.zero
        P.negative = DBR.negative
    }

    /** push relative address - pushes address relative from current PC (see operand) */
    private fun instPER() {
        S.pushShort(fetchShort() + PC.value)
    }

    /** Load/Set value of Y */
    private fun instLDY(value: Int) {
        Y.set(value)
        P.zero = Y.zero
        P.negative = Y.negative
    }

    /** Load/Set value of A */
    private fun instLDA(value: Int) {
        A.set(value)
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Load/Set value of X */
    private fun instLDX(value: Int) {
        X.set(value)
        P.zero = X.zero
        P.negative = X.negative
    }

    /** Exchange B with A - swaps the 2 bytes of A */
    private fun instXBA() {
        A.xba()
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Transfer A to S */
    private fun instTCS() {
        S.set(A.get())
    }

    /** Transfer S to A also setting flags */
    private fun instTSC() {
        A.value = S.value
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Transfer A to D */
    private fun instTCD() {
        D.value = A.value
        P.zero = D.value == 0
        P.negative = D.negative
    }

    /** Transfer X to A */
    private fun instTXA() {
        A.set(X.value)
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Transfer Y to A */
    private fun instTYA() {
        A.set(Y.value)
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Transfer X to S */
    private fun instTXS() {
        S.set(X.value)
    }

    /** Transfer X to Y */
    private fun instTXY() {
        Y.set(X.value)
        P.zero = Y.zero
        P.negative = Y.negative
    }

    /** Transfer A to Y */
    private fun instTAY() {
        Y.set(A.value)
        P.zero = Y.zero
        P.negative = Y.negative
    }

    /** Transfer A to X */
    private fun instTAX() {
        X.set(A.value)
        P.zero = X.zero
        P.negative = X.negative
    }

    /** Transfer D to A */
    private fun instTDC() {
        A.value = D.value
        P.zero = A.zero
        P.negative = A.negative
    }

    /** Transfer S to X */
    private fun instTSX() {
        X.set(S.value)
        P.zero = X.zero
        P.negative = X.negative
    }

    /** Transfer Y to X */
    private fun instTYX() {
        X.set(Y.value)
        P.zero = X.zero
        P.negative = X.negative
    }

    /** store zero */
    private fun instSTZ(address: FullAddress) {
        memory.writeFor(A, address, 0)
    }

    /** Store A at given address */
    private fun instSTA(address: FullAddress) {
        memory.writeFor(A, address)
    }

    /** Store Y at given address */
    private fun instSTY(address: FullAddress) {
        memory.writeFor(Y, address)
    }

    /** Store X at given address */
    private fun instSTX(address: FullAddress) {
        memory.writeFor(X, address)
    }

    /** Block move positive */
    private fun instMVP() {
        // TODO
        error("not implemented yet")
    }

    /** Block move negative */
    private fun instMVN() {
        // TODO
        error("not implemented yet")
    }

    /** Jump to subroutine (given address without bank) - pushes the current PC minus 1 */
    private fun instJSR(address: FullAddress) {
        S.pushShort(PC.value - 1)
        PC.value = address.address
    }

    /** Jump to subroutine (given address including bank), pushes the current PC minus 1 and the PBR */
    private fun instJSL(address: FullAddress) {
        S.pushByte(PBR.value)
        S.pushShort(PC.value - 1)
        PC.value = address.address
        PBR.value = address.bankNo
    }

    /** Jump to full address including bank */
    private fun instJMP(address: FullAddress) {
        PC.value = address.address
        PBR.value = address.bankNo
    }

    /** Jump Long - jump to the given address bank included */
    private fun instJML(address: FullAddress) {
        PC.value = address.address
        PBR.value = address.bankNo
    }

    /** return from interrupt */
    private fun instRTI() {
        // TODO
        error("not implemented yet")
    }

    /** return from subroutine */
    private fun instRTS() {
        // TODO
        error("not implemented yet")
    }

    /** Return from Subroutine long */
    private fun instRTL() {
        // TODO
        error("not implemented yet")
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

    /**
     * the accumulator is a special 16-bit-register.
     * if it is not in 8-bit-mode it is a normal 16-bit-register.
     * if it is in 8-bit-mode, all set-operatioins preserve the upper byte, so that it can be used as temporary
     * storage using the XBA-isntruction
     */
    inner class Accumulator : DiffSizeRegister() {
        override fun shallBe8Bit(): Boolean {
            // 16-bit mode is in native mode if the memory-bit (M) is not set
            return mode == ProcessorMode.EMULATION || P.accumulator
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
            return mode == ProcessorMode.EMULATION || P.index
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
    inner class StackPointer(
    ) : DiffSizeRegister() {
        override fun shallBe8Bit(): Boolean {
            return mode == ProcessorMode.EMULATION
        }

        override fun checkValue(value: Int): Int {
            // when in 8-bit-mode the upper byte is forced to always be 1
            return super.checkValue(value) + if (_8bitMode) 0x100 else 0
        }

        fun pushByte(value: Int) {
            memory.writeByte(0, this.value, value)
            dec()
        }

        fun pushShort(value: Int) {
            pushByte(value shr 8)
            pushByte(value)
        }

        fun pullByte(): Int {
            inc()
            return memory.readByte(0, value)
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

        private const val COP_VECTOR_ADDRESS: ShortAddress = 0xFFF4
        private const val NATIVE_BRK_VECTOR_ADDRESS: ShortAddress = 0xFFF6
        private const val ABORT_VECTOR_ADDRESS: ShortAddress = 0xFFF8
        private const val NMI_VECTOR_ADDRESS: ShortAddress = 0xFFFA
        private const val RESET_VECTOR_ADDRESS: ShortAddress = 0xFFFC
        private const val IRQ_VECTOR_ADDRESS: ShortAddress = 0xFFFE
        private const val EMULATION_BRK_VECTOR_ADDRESS: ShortAddress = IRQ_VECTOR_ADDRESS

        private inline operator fun Int.plus(s: StackPointer) = if(s._8bitMode) this + s.value and 0xFF else this + s.value
        private inline operator fun Int.plus(a: Accumulator) = if (a._8bitMode) this + a.value and 0xFF else this + a.value
        private inline operator fun Int.plus(r: Register) = this + r.value

        private fun Memory.readFor(register: DiffSizeRegister, address: FullAddress) = if(register._8bitMode) readByte(address.bankNo, address.address) else readShort(address.bankNo, address.address)
        private fun Memory.writeFor(register: DiffSizeRegister, address: FullAddress, value: Int = register.value) = if (register._8bitMode) writeByte(address.bankNo, address.address, value) else writeShort(address.bankNo, address.address, value)
    }
}
