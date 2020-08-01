package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProcessorOperationsBranchTest {
    @Nested
    inner class Bcc : OperationTest(
        "BCC",
        { bcc }
    ) {
        @Test
        fun branch_not() {
            status.carry = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.carry = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Bcs : OperationTest(
        "BCS",
        { bcs }
    ) {
        @Test
        fun branch_not() {
            status.carry = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.carry = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Beq : OperationTest(
        "BEQ",
        { beq }
    ) {
        @Test
        fun branch_not() {
            status.zero = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.zero = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Bmi : OperationTest(
        "BMI",
        { bmi }
    ) {
        @Test
        fun branch_not() {
            status.negative = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.negative = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Bne : OperationTest(
        "BNE",
        { bne }
    ) {
        @Test
        fun branch_not() {
            status.zero = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.zero = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Bpl : OperationTest(
        "BPL",
        { bpl }
    ) {
        @Test
        fun branch_not() {
            status.negative = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.negative = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Bra : OperationTest(
        "BRA",
        { bra }
    ) {
        @Test
        fun branch_P0() {
            status.set(0)
            status.emulationMode = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }

        @Test
        fun branch_PFF() {
            status.set(0xFF)

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Brl : OperationTest(
        "BRL",
        { brl }
    ) {
        @Test
        fun branch_P0() {
            status.set(0)
            status.emulationMode = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.FULLADDRESS
            addressMode.fetchNext(0xFFFFFF)

            testOperation(pc = 0xFFFF, pbr = 0xFF)
        }

        @Test
        fun branch_PFF() {
            status.set(0xFF)

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.FULLADDRESS
            addressMode.fetchNext(0xFFFFFF)

            testOperation(pc = 0xFFFF, pbr = 0xFF)
        }
    }

    @Nested
    inner class Bvc : OperationTest(
        "BVC",
        { bvc }
    ) {
        @Test
        fun branch_not() {
            status.overflow = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.overflow = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Bvs : OperationTest(
        "BVS",
        { bvs }
    ) {
        @Test
        fun branch_not() {
            status.overflow = false

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x00)

            testOperation(pc = 0x1234)
        }

        @Test
        fun branch() {
            status.overflow = true

            prepareProcessor(pc = 0x1234)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0xFFFF)

            testOperation(pc = 0xFFFF)
        }
    }

    @Nested
    inner class Jml : OperationTest(
        "JML",
        { jml }
    ) {
        @Test
        fun jump() {
            addressMode.result = AddressModeResult.FULLADDRESS
            addressMode.fetchNext(0x010203)

            testOperation(pc = 0x0203, pbr = 0x01)
        }
    }

    @Nested
    inner class Jmp : OperationTest(
        "JMP",
        { jmp }
    ) {
        @Test
        fun jump() {
            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x0203)

            testOperation(pc = 0x0203)
        }

        @Test
        fun jump_alwaysShort() {
            addressMode.result = AddressModeResult.FULLADDRESS
            addressMode.fetchNext(0x010203)

            testOperation(pc = 0x0203)
        }
    }

    @Nested
    inner class Jsl : OperationTest(
        "JSL",
        { jsl }
    ) {
        @Test
        fun jump() {
            prepareStatus(s16 = true)
            prepareProcessor(pc = 0x1234, pbr = 0x56, s = 0xFFFF)

            addressMode.result = AddressModeResult.FULLADDRESS
            addressMode.fetchNext(0x112233)

            memory.expectWrite(0x00, 0xFFFF, 0x56)
            memory.expectWrite(0x00, 0xFFFE, 0x12)
            memory.expectWrite(0x00, 0xFFFD, 0x33)

            testOperation(pc = 0x2233, pbr = 0x11, s = 0xFFFC)
        }
    }

    @Nested
    inner class Jsr : OperationTest(
        "JSR",
        { jsr }
    ) {
        @Test
        fun jump() {
            prepareStatus(s16 = true)
            prepareProcessor(pc = 0x1234, s = 0xFFFF)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x1122)

            memory.expectWrite(0x00, 0xFFFF, 0x12)
            memory.expectWrite(0x00, 0xFFFE, 0x33)

            testOperation(pc = 0x1122, s = 0xFFFD)
        }

        @Test
        fun jump_alwaysShort() {
            prepareStatus(s16 = true)
            prepareProcessor(pc = 0x1234, s = 0xFFFF)

            addressMode.result = AddressModeResult.FULLADDRESS
            addressMode.fetchNext(0x112233)

            memory.expectWrite(0x00, 0xFFFF, 0x12)
            memory.expectWrite(0x00, 0xFFFE, 0x33)

            testOperation(pc = 0x2233, s = 0xFFFD)
        }
    }
}