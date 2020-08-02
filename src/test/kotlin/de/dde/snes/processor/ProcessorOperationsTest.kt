package de.dde.snes.processor

import de.dde.snes.processor.addressmode.AddressModeResult
import de.dde.snes.processor.operation.OperationTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsTest {
    @Nested
    inner class Adc : OperationTest(
        "ADC",
        { adc }
    ) {
        @Test
        fun test() {
            TODO()
        }
    }

    @Nested
    inner class Brk : OperationTest(
        "BRK",
        { brk }
    ) {
        @Test
        fun test() {
            TODO()
        }
    }

    @Nested
    inner class Cop : OperationTest(
        "COP",
        { cop }
    ) {
        @Test
        fun test() {
            TODO()
        }
    }

    @Nested
    inner class Mvn : OperationTest(
        "MVN",
        { mvn }
    ) {
        @Test
        fun accumulatorAlwaysConsidered16Bit() {
            prepareStatus(a16 = false, i16 = false)
            prepareProcessor(pc = 0x1000, a = 1, x = 2, y = 3)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 2, 4)
            memory.expectWrite(0x20, 3, 4)

            memory.returnFor(0x10, 1, 5)
            memory.expectWrite(0x20, 2, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0, y = 1)
        }

        @Test
        fun bigIndex() {
            prepareStatus(a16 = true, i16 = true)

            prepareProcessor(pc = 0x1000, a = 1, x = 0x1002, y = 0x1003)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 0x1002, 4)
            memory.expectWrite(0x20, 0x1003, 4)

            memory.returnFor(0x10, 0x1001, 5)
            memory.expectWrite(0x20, 0x1002, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0x1000, y = 0x1001)
        }

        @Test
        fun indexWrapAround() {
            prepareStatus(a16 = true, i16 = false)
            prepareProcessor(pc = 0x1000, a = 1, x = 1, y = 0)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 1, 4)
            memory.expectWrite(0x20, 0, 4)

            memory.returnFor(0x10, 0, 5)
            memory.expectWrite(0x20, 0xFF, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0xFF, y = 0xFE)
        }

        @Test
        fun bigIndexWrapAround() {
            prepareStatus(a16 = true, i16 = true)
            prepareProcessor(pc = 0x1000, a = 1, x = 1, y = 0)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 1, 4)
            memory.expectWrite(0x20, 0, 4)

            memory.returnFor(0x10, 0, 5)
            memory.expectWrite(0x20, 0xFFFF, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0xFFFF, y = 0xFFFE)
        }
    }

    @Nested
    inner class Mvp : OperationTest(
        "MVP",
        { mvp }
    ) {
        @Test
        fun accumulatorAlwaysConsidered16Bit() {
            prepareStatus(a16 = false, i16 = false)
            prepareProcessor(pc = 0x1000, a = 1, x = 2, y = 3)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 2, 4)
            memory.expectWrite(0x20, 3, 4)

            memory.returnFor(0x10, 3, 5)
            memory.expectWrite(0x20, 4, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 4, y = 5)
        }

        @Test
        fun bigIndex() {
            prepareStatus(a16 = true, i16 = true)

            prepareProcessor(pc = 0x1000, a = 1, x = 0x1002, y = 0x1003)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 0x1002, 4)
            memory.expectWrite(0x20, 0x1003, 4)

            memory.returnFor(0x10, 0x1003, 5)
            memory.expectWrite(0x20, 0x1004, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0x1004, y = 0x1005)
        }

        @Test
        fun indexWrapAround() {
            prepareStatus(a16 = true, i16 = false)
            prepareProcessor(pc = 0x1000, a = 1, x = 0xFF, y = 0xFE)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 0xFF, 4)
            memory.expectWrite(0x20, 0xFE, 4)

            memory.returnFor(0x10, 0x00, 5)
            memory.expectWrite(0x20, 0xFF, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0x01, y = 0x00)
        }

        @Test
        fun bigIndexWrapAround() {
            prepareStatus(a16 = true, i16 = true)
            prepareProcessor(pc = 0x1000, a = 1, x = 0xFFFF, y = 0xFFFE)

            addressMode.result == AddressModeResult.IMMEDIATE
            addressMode.fetchNext(0x20)
            addressMode.fetchNext(0x10)

            memory.returnFor(0x10, 0xFFFF, 4)
            memory.expectWrite(0x20, 0xFFFE, 4)

            memory.returnFor(0x10, 0x00, 5)
            memory.expectWrite(0x20, 0xFFFF, 5)

            testOperation(pc = 0x1000, a = 0xFFFF, dbr = 0x20, x = 0x0001, y = 0x0000)
        }
    }

    @Nested
    inner class Nop : OperationTest(
        "NOP",
        { nop }
    ) {
        @Test
        fun test() {
            prepareProcessor(pc = 1, pbr = 2, dbr = 3, a = 4, x = 5, y = 6, s = 7)
            testOperation(pc = 1, pbr = 2, dbr = 3, a = 4, x = 5, y = 6, s = 7)
        }
    }
}