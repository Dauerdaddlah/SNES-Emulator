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
}