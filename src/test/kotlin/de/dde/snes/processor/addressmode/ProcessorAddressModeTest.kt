package de.dde.snes.processor.addressmode

import de.dde.snes.memory.*
import de.dde.snes.processor.Processor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessorAddressModeTest {
    // TODO test all addressmodes wich use X and Y with 8 and 16-bit
    lateinit var memory: TestMemory
    lateinit var processor: Processor

    @BeforeEach
    fun initialize() {
        memory = TestMemory()
        processor = Processor(memory)
    }

    private fun prepareProcessor(
        pc: Int,
        pbr: Int,
        dbr: Int,
        a16: Boolean = false, a: Int = 0,
        i16: Boolean = false, x: Int = 0, y: Int = 0,
        d: Int = 0,
        s16: Boolean = false, s: Int = 0,
        p: Int = 0) {

        processor.rPC.set(pc)
        processor.rPBR.set(pbr)
        processor.rDBR.set(dbr)
        processor.rA.size16Bit = a16
        processor.rA.setFull(a)
        processor.rX.size16Bit = i16
        processor.rX.set(x)
        processor.rY.size16Bit = i16
        processor.rY.set(y)
        processor.rD.set(d)
        processor.rS.size16Bit = s16
        processor.rS.set(s)
        processor.rP.set(p)
    }

    private fun testMode(mode: AddressMode, symbol: String, result: AddressModeResult, fetch: Int) {
        Assertions.assertEquals(symbol, mode.symbol, "wrong symbol")
        Assertions.assertEquals(result, mode.result, "wrong result")
        if (result == AddressModeResult.NOTHING) {
            Assertions.assertThrows(java.lang.Exception::class.java, { mode.fetchValue() }, "Non-fetchable mode could be fetched")

        } else {
            Assertions.assertEquals(fetch, mode.fetchValue(), "wrong result fetched")
        }
        memory.checkResult()
    }

    @Test
    fun test_absolute() {
        prepareProcessor(0x28, 4, 3)

        memory.returnFor(4, 0x28, 0x12, true)
        memory.returnFor(4, 0x29, 0x34, true)

        testMode(processor.addressModes.absolute, "a", AddressModeResult.ADDRESS_DBR, 0x3412)
    }

    @Test
    fun test_absoluteIndexedIndirect() {
        prepareProcessor(0x12, 1, 3, i16 = false, x = 0x55)

        memory.returnFor(1, 0x12, 0x22)
        memory.returnFor(1, 0x13, 0x33)

        memory.returnFor(0, 0x3377, 1)
        memory.returnFor(0, 0x3378, 2)

        testMode(processor.addressModes.absoluteIndexedIndirect, "(a,x)", AddressModeResult.SHORTADDRESS, 0x0201)
    }

    @Test
    fun test_absoluteIndexedWithX() {
        prepareProcessor(0x9, 5, 4, i16 = false, x = 0x14, y = 0x28)

        memory.returnFor(5, 0x9, 0x5)
        memory.returnFor(5, 0xA, 0xAA)

        testMode(processor.addressModes.absoluteIndexedWithX, "a,x", AddressModeResult.ADDRESS_DBR, 0xAA19)
    }

    @Test
    fun test_absoluteIndexedWithY() {
        prepareProcessor(0x9, 5, 4, i16 = false, x = 0x14, y = 0x28)

        memory.returnFor(5, 0x9, 0x5)
        memory.returnFor(5, 0xA, 0xAA)

        testMode(processor.addressModes.absoluteIndexedWithY, "a,y", AddressModeResult.ADDRESS_DBR, 0xAA2D)
    }

    @Test
    fun test_absoluteIndirect() {
        prepareProcessor(0x4685, 0x12, 0x3)

        memory.returnFor(0x12, 0x4685, 0x5)
        memory.returnFor(0x12, 0x4686, 0x6)

        memory.returnFor(0, 0x0605, 3)
        memory.returnFor(0, 0x0606, 4)

        testMode(processor.addressModes.absoluteIndirect, "(a)", AddressModeResult.SHORTADDRESS, 0x0403)
    }

    @Test
    fun test_absoluteIndirectLong() {
        prepareProcessor(0x4685, 0x12, 0x3)

        memory.returnFor(0x12, 0x4685, 0x5)
        memory.returnFor(0x12, 0x4686, 0x6)

        memory.returnFor(0, 0x0605, 3)
        memory.returnFor(0, 0x0606, 4)
        memory.returnFor(0, 0x0607, 5)

        testMode(processor.addressModes.absoluteIndirectLong, "(a)", AddressModeResult.FULLADDRESS, 0x050403)
    }

    @Test
    fun test_absoluteLongIndexedWithX() {
        prepareProcessor(0x1234, 0x22, 3, i16 = false, x = 0x3)

        memory.returnFor(0x22, 0x1234, 1)
        memory.returnFor(0x22, 0x1235, 2)
        memory.returnFor(0x22, 0x1236, 3)

        testMode(processor.addressModes.absoluteLongIndexedWithX, "al,x", AddressModeResult.FULLADDRESS, 0x030204)
    }

    @Test
    fun test_absoluteLong() {
        prepareProcessor(0x33, 6, 3)

        memory.returnFor(6, 0x33, 0x11, true)
        memory.returnFor(6, 0x34, 0x22, true)
        memory.returnFor(6, 0x35, 0x33, true)

        testMode(processor.addressModes.absoluteLong, "al", AddressModeResult.FULLADDRESS, 0x332211)
    }

    @Test
    fun test_Accumulator_8Bit() {
        prepareProcessor(1, 2, 3, a16 = false, a = 0x1234)

        testMode(processor.addressModes.accumulator, "A", AddressModeResult.ACCUMULATOR, 0x34)
    }

    @Test
    fun test_Accumulator_16Bit() {
        prepareProcessor(1, 2, 3, a16 = true, a = 0x1234)

        testMode(processor.addressModes.accumulator, "A", AddressModeResult.ACCUMULATOR, 0x1234)
    }

    @Test
    fun test_BlockMove() {
        testMode(processor.addressModes.blockMove, "xyc", AddressModeResult.NOTHING, -1)
    }

    @Test
    fun test_directIndexedIndirect() {
        prepareProcessor(0x12, 5, 7, i16 = false, x = 8, d = 0x11)

        memory.returnFor(5, 0x12, 6)

        memory.returnFor(0, 0x1F, 3)
        memory.returnFor(0, 0x20, 4)

        testMode(processor.addressModes.directIndexedIndirect, "(d,x)", AddressModeResult.ADDRESS_DBR, 0x0403)
    }

    @Test
    fun test_directIndexedWithX() {
        prepareProcessor(0x8000, 2, 6, i16 = false, x = 0x94, y = 0x86, d = 3)

        memory.returnFor(2, 0x8000, 1)

        testMode(processor.addressModes.directIndexedWithX, "d,x", AddressModeResult.ADDRESS_0, 0x98)
    }

    @Test
    fun test_directIndexedWithY() {
        prepareProcessor(0x8000, 2, 6, i16 = false, x = 0x94, y = 0x86, d = 3)

        memory.returnFor(2, 0x8000, 1)

        testMode(processor.addressModes.directIndexedWithY, "d,y", AddressModeResult.ADDRESS_0, 0x8A)
    }

    @Test
    fun test_directIndirectIndexed() {
        prepareProcessor(0x1234, 1, 2, i16 = false, x = 0x4, y = 0x12, d = 0x23)

        memory.returnFor(1, 0x1234, 0x80)

        memory.returnFor(0, 0xA3, 9)
        memory.returnFor(0, 0xA4, 0xA)

        testMode(processor.addressModes.directIndirectIndexed, "(d),y", AddressModeResult.FULLADDRESS, 0x020A1B)
    }

    @Test
    fun test_directIndirectIndexed_WithBankChange() {
        prepareProcessor(0x1234, 1, 2, i16 = false, x = 0x4, y = 0x12, d = 0x23)

        memory.returnFor(1, 0x1234, 0x80)

        memory.returnFor(0, 0xA3, 0xFF)
        memory.returnFor(0, 0xA4, 0xFF)

        testMode(processor.addressModes.directIndirectIndexed, "(d),y", AddressModeResult.FULLADDRESS, 0x030011)
    }

    @Test
    fun test_directIndirectLongIndexed() {
        prepareProcessor(0x2222, 4, 6, i16 = false, x = 3, y = 4, d = 0x20)

        memory.returnFor(4, 0x2222, 0x10)

        memory.returnFor(0, 0x30, 0x1)
        memory.returnFor(0, 0x31, 0x2)
        memory.returnFor(0, 0x32, 0x3)

        testMode(processor.addressModes.directIndirectLongIndexed, "[d],y", AddressModeResult.FULLADDRESS, 0x030205)
    }

    @Test
    fun test_directIndirectLongIndexed_withBankChange() {
        prepareProcessor(0x2222, 4, 6, i16 = false, x = 3, y = 4, d = 0x20)

        memory.returnFor(4, 0x2222, 0x10)

        memory.returnFor(0, 0x30, 0xFF)
        memory.returnFor(0, 0x31, 0xFF)
        memory.returnFor(0, 0x32, 0x3)

        testMode(processor.addressModes.directIndirectLongIndexed, "[d],y", AddressModeResult.FULLADDRESS, 0x040003)
    }

    @Test
    fun test_directIndirectLong() {
        prepareProcessor(0xAAAA, 1, 2, d = 0x1234)

        memory.returnFor(1, 0xAAAA, 5)

        memory.returnFor(0, 0x1239, 1)
        memory.returnFor(0, 0x123A, 2)
        memory.returnFor(0, 0x123B, 3)

        testMode(processor.addressModes.directIndirectLong, "[d]", AddressModeResult.FULLADDRESS, 0x030201)
    }

    @Test
    fun test_directIndirect() {
        prepareProcessor(0x9876, 0x10, 0x11, d = 0x21)

        memory.returnFor(0x10, 0x9876, 0x12)

        memory.returnFor(0, 0x33, 0x44)
        memory.returnFor(0, 0x34, 0x55)

        testMode(processor.addressModes.directIndirect, "(d)", AddressModeResult.ADDRESS_DBR, 0x5544)
    }

    @Test
    fun test_direct() {
        prepareProcessor(0x5555, 2, 3, d = 0x1234)

        memory.returnFor(2, 0x5555, 6)

        testMode(processor.addressModes.direct, "d", AddressModeResult.ADDRESS_0, 0x123A)
    }

    @Test
    fun test_immediate() {
        prepareProcessor(0x1111, 0x22, 0x33)

        memory.returnFor(0x22, 0x1111, 0x44)

        testMode(processor.addressModes.immediate, "#", AddressModeResult.IMMEDIATE, 0x44)
    }

    @Test
    fun test_implied() {
        testMode(processor.addressModes.implied, "i", AddressModeResult.NOTHING, -1)
    }

    @Test
    fun test_programCounterRelativeLong() {
        prepareProcessor(0x1111, 0x22, 0x33)

        memory.returnFor(0x22, 0x1111, 1)
        memory.returnFor(0x22, 0x1112, 2)

        testMode(processor.addressModes.programCounterRelativeLong, "rl", AddressModeResult.SHORTADDRESS, 0x1314)
    }

    @Test
    fun test_programCounterRelativeLong_withNegative() {
        prepareProcessor(0x1997, 0x22, 0x33)

        memory.returnFor(0x22, 0x1997, 0xFD)
        memory.returnFor(0x22, 0x1998, 0xFF)

        // 6553
        // -3
        testMode(processor.addressModes.programCounterRelativeLong, "rl", AddressModeResult.SHORTADDRESS, 0x1996)
    }

    @Test
    fun test_programCounterRelative() {
        prepareProcessor(0x1998, 0x33, 0x44)

        memory.returnFor(0x33, 0x1998, 0x3)

        testMode(processor.addressModes.programCounterRelative, "r", AddressModeResult.SHORTADDRESS, 0x199C)
    }

    @Test
    fun test_programCounterRelative_withNegative() {
        prepareProcessor(0x1998, 0x33, 0x44)

        memory.returnFor(0x33, 0x1998, 0xFD)

        testMode(processor.addressModes.programCounterRelative, "r", AddressModeResult.SHORTADDRESS, 0x1996)
    }

    @Test
    fun test_stack() {
        testMode(processor.addressModes.stack, "s", AddressModeResult.NOTHING, -1)
    }

    @Test
    fun test_stackRelative() {
        prepareProcessor(0x1234, 0x12, 0x34, s16 = true, s = 0x0123)

        memory.returnFor(0x12, 0x1234, 0x12)

        testMode(processor.addressModes.stackRelative, "d,s", AddressModeResult.ADDRESS_0, 0x0135)
    }

    @Test
    fun test_stackRelativeIndirectIndexed() {
        prepareProcessor(0x1111, 1, 2, s16 = true, s = 0x2222, i16 = false, x = 0x33, y = 0x44)

        memory.returnFor(1, 0x1111, 0x11)

        memory.returnFor(0, 0x2233, 0x22)
        memory.returnFor(0, 0x2234, 0x11)

        testMode(processor.addressModes.stackRelativeIndirectIndexed, "(d,s),y", AddressModeResult.FULLADDRESS, 0x021166)
    }

    @Test
    fun test_stackRelativeIndirectIndexed_withBankChange() {
        prepareProcessor(0x1111, 1, 2, s16 = true, s = 0x2222, i16 = false, x = 0x33, y = 0x44)

        memory.returnFor(1, 0x1111, 0x11)

        memory.returnFor(0, 0x2233, 0xFF)
        memory.returnFor(0, 0x2234, 0xFF)

        testMode(processor.addressModes.stackRelativeIndirectIndexed, "(d,s),y", AddressModeResult.FULLADDRESS, 0x030043)
    }
}