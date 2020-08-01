package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsRegisterTest {
    @Nested
    inner class Lda : OperationTest(
        "LDA",
        { lda }
    ) {
        @Nested
        inner class Lda8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(a = 0x11)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, 0x22)

                testOperation(a = 0x22)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0x11)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)

                testOperation(a = 0x22)
            }

            @Test
            fun onlySetLowByte() {
                prepareProcessor(a = 0x1111)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)

                testOperation(a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0, true, false",
                "0xFF, false, true",
                "0x80, false, true",
                "0x7F, false, false"
            )
            fun status(a: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(a = 0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(a)

                status.zero = zero
                status.negative = negative

                testOperation(a = a)
            }
        }

        @Nested
        inner class Lda16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(a = 0x1111)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, 0x22)
                memory.returnFor(0x00, 0x1112, 0x33)

                testOperation(a = 0x3322)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0x11)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)
                addressMode.fetchNext(0x33)

                testOperation(a = 0x3322)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, true, false",
                "0xFF, 0xFF, 0xFFFF, false, true",
                "0x00, 0x80, 0x8000, false, true",
                "0xFF, 0x7F, 0x7FFF, false, false"
            )
            fun status(fetch1: Int, fetch2: Int, a: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(a = 0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.zero = zero
                status.negative = negative

                testOperation(a = a)
            }
        }
    }

    @Nested
    inner class Ldx : OperationTest(
        "LDX",
        { ldx }
    ) {
        @Nested
        inner class Ldx8 : Test8BitIndex() {
            @Test
            fun address() {
                prepareProcessor(x = 0x11)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, 0x22)

                testOperation(x = 0x22)
            }

            @Test
            fun immediate() {
                prepareProcessor(x = 0x11)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)

                testOperation(x = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0, true, false",
                "0xFF, false, true",
                "0x80, false, true",
                "0x7F, false, false"
            )
            fun status(x: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = 0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(x)

                status.zero = zero
                status.negative = negative

                testOperation(x = x)
            }
        }

        @Nested
        inner class Ldx16 : Test16BitIndex() {
            @Test
            fun address() {
                prepareProcessor(x = 0x1111)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, 0x22)
                memory.returnFor(0x00, 0x1112, 0x33)

                testOperation(x = 0x3322)
            }

            @Test
            fun immediate() {
                prepareProcessor(x = 0x11)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)
                addressMode.fetchNext(0x33)

                testOperation(x = 0x3322)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, true, false",
                "0xFF, 0xFF, 0xFFFF, false, true",
                "0x00, 0x80, 0x8000, false, true",
                "0xFF, 0x7F, 0x7FFF, false, false"
            )
            fun status(fetch1: Int, fetch2: Int, x: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = 0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.zero = zero
                status.negative = negative

                testOperation(x = x)
            }
        }
    }

    @Nested
    inner class Ldy : OperationTest(
        "LDY",
        { ldy }
    ) {
        @Nested
        inner class Ldy8 : Test8BitIndex() {
            @Test
            fun address() {
                prepareProcessor(y = 0x11)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, 0x22)

                testOperation(y = 0x22)
            }

            @Test
            fun immediate() {
                prepareProcessor(y = 0x11)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)

                testOperation(y = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0, true, false",
                "0xFF, false, true",
                "0x80, false, true",
                "0x7F, false, false"
            )
            fun status(y: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = 0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(y)

                status.zero = zero
                status.negative = negative

                testOperation(y = y)
            }
        }

        @Nested
        inner class Ldy16 : Test16BitIndex() {
            @Test
            fun address() {
                prepareProcessor(y = 0x1111)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, 0x22)
                memory.returnFor(0x00, 0x1112, 0x33)

                testOperation(y = 0x3322)
            }

            @Test
            fun immediate() {
                prepareProcessor(y = 0x11)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)
                addressMode.fetchNext(0x33)

                testOperation(y = 0x3322)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, true, false",
                "0xFF, 0xFF, 0xFFFF, false, true",
                "0x00, 0x80, 0x8000, false, true",
                "0xFF, 0x7F, 0x7FFF, false, false"
            )
            fun status(fetch1: Int, fetch2: Int, y: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = 0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.zero = zero
                status.negative = negative

                testOperation(y = y)
            }
        }
    }
}