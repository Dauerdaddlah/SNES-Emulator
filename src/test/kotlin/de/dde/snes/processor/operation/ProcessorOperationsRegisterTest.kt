package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsRegisterTest {
    @Nested
    inner class Dec : OperationTest(
        "DEC",
        { dec }
    ) {
        @Nested
        inner class Dec8 : Test8Bit() {
            @Test
            fun accumulator() {
                prepareProcessor(a = 0x02)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                testOperation(a = 0x01)
            }

            @Test
            fun accumulator_ignoreHighByte() {
                prepareProcessor(a = 0x0000)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x00)
                status.negative = true

                testOperation(a = 0x00FF)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x02)

                memory.expectWrite(0x01, 0x1111, 0x01)

                testOperation(dbr = 0x01)
            }

            @Test
            fun address_Only1Byte() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x00)

                memory.expectWrite(0x01, 0x1111, 0xFF)

                status.negative = true

                testOperation(dbr = 0x01)
            }

            @ParameterizedTest
            @CsvSource(
                "0x01, 0x00, true, false",
                "0x00, 0xFF, false, true",
                "0x81, 0x80, false, true",
                "0x80, 0x7F, false, false"
            )
            fun status(a: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(a)

                status.zero = zero
                status.negative = negative

                testOperation(a = result)
            }
        }

        @Nested
        inner class Dec16 : Test16Bit() {
            @Test
            fun accumulator() {
                prepareProcessor(a = 0x0202)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x0202)

                testOperation(a = 0x0201)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x02)
                memory.returnFor(0x01, 0x1112, 0x02)

                memory.expectWrite(0x01, 0x1111, 0x01)
                memory.expectWrite(0x01, 0x1112, 0x02)

                testOperation(dbr = 0x01)
            }

            @Test
            fun address_alwaysWrite2Byte() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x02)
                memory.returnFor(0x01, 0x1112, 0x00)

                memory.expectWrite(0x01, 0x1111, 0x01)
                memory.expectWrite(0x01, 0x1112, 0x00)

                testOperation(dbr = 0x01)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0001, 0x0000, true, false",
                "0x0000, 0xFFFF, false, true",
                "0x8001, 0x8000, false, true",
                "0x8000, 0x7FFF, false, false"
            )
            fun status(a: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(a)

                status.zero = zero
                status.negative = negative

                testOperation(a = result)
            }
        }
    }

    @Nested
    inner class Dex : OperationTest(
        "DEX",
        { dex }
    ) {
        @Nested
        inner class Dex8 : Test8BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x01, 0x00, true, false",
                "0x00, 0xFF, false, true",
                "0x81, 0x80, false, true",
                "0x80, 0x7F, false, false"
            )
            fun status(x: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = result)
            }
        }

        @Nested
        inner class Dex16 : Test16BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x0001, 0x0000, true, false",
                "0x0000, 0xFFFF, false, true",
                "0x8001, 0x8000, false, true",
                "0x8000, 0x7FFF, false, false"
            )
            fun status(x: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = result)
            }
        }
    }

    @Nested
    inner class Dey : OperationTest(
        "DEY",
        { dey }
    ) {
        @Nested
        inner class Dey8 : Test8BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x01, 0x00, true, false",
                "0x00, 0xFF, false, true",
                "0x81, 0x80, false, true",
                "0x80, 0x7F, false, false"
            )
            fun status(y: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = result)
            }
        }

        @Nested
        inner class Dey16 : Test16BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x0001, 0x0000, true, false",
                "0x0000, 0xFFFF, false, true",
                "0x8001, 0x8000, false, true",
                "0x8000, 0x7FFF, false, false"
            )
            fun status(y: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = result)
            }
        }
    }

    @Nested
    inner class Inc : OperationTest(
        "INC",
        { inc }
    ) {
        @Nested
        inner class Inc8 : Test8Bit() {
            @Test
            fun accumulator() {
                prepareProcessor(a = 0x00)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x00)

                testOperation(a = 0x01)
            }

            @Test
            fun accumulator_ignoreHighByte() {
                prepareProcessor(a = 0x00FF)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0xFF)
                status.zero = true

                testOperation(a = 0x0000)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x00)

                memory.expectWrite(0x01, 0x1111, 0x01)

                testOperation(dbr = 0x01)
            }

            @Test
            fun address_Only1Byte() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0xFF)

                memory.expectWrite(0x01, 0x1111, 0x00)

                status.zero = true

                testOperation(dbr = 0x01)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, 0x01, false, false",
                "0xFF, 0x00, true, false",
                "0x7F, 0x80, false, true",
                "0x80, 0x81, false, true"
            )
            fun status(a: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(a)

                status.zero = zero
                status.negative = negative

                testOperation(a = result)
            }
        }

        @Nested
        inner class Inc16 : Test16Bit() {
            @Test
            fun accumulator() {
                prepareProcessor(a = 0x0200)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x0200)

                testOperation(a = 0x0201)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x00)
                memory.returnFor(0x01, 0x1112, 0x02)

                memory.expectWrite(0x01, 0x1111, 0x01)
                memory.expectWrite(0x01, 0x1112, 0x02)

                testOperation(dbr = 0x01)
            }

            @Test
            fun address_alwaysWrite2Byte() {
                prepareProcessor(dbr = 0x01)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x01, 0x1111, 0x00)
                memory.returnFor(0x01, 0x1112, 0x00)

                memory.expectWrite(0x01, 0x1111, 0x01)
                memory.expectWrite(0x01, 0x1112, 0x00)

                testOperation(dbr = 0x01)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0001, false, false",
                "0xFFFF, 0x0000, true, false",
                "0x8000, 0x8001, false, true",
                "0x7FFF, 0x8000, false, true"
            )
            fun status(a: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(a)

                status.zero = zero
                status.negative = negative

                testOperation(a = result)
            }
        }
    }

    @Nested
    inner class Inx : OperationTest(
        "INX",
        { inx }
    ) {
        @Nested
        inner class Inx8 : Test8BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x00, 0x01, false, false",
                "0xFF, 0x00, true, false",
                "0x80, 0x81, false, true",
                "0x7F, 0x80, false, true"
            )
            fun status(x: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = result)
            }
        }

        @Nested
        inner class Inx16 : Test16BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0001, false, false",
                "0xFFFF, 0x0000, true, false",
                "0x8000, 0x8001, false, true",
                "0x7FFF, 0x8000, false, true"
            )
            fun status(x: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = result)
            }
        }
    }

    @Nested
    inner class Iny : OperationTest(
        "INY",
        { iny }
    ) {
        @Nested
        inner class Iny8 : Test8BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x00, 0x01, false, false",
                "0xFF, 0x00, true, false",
                "0x80, 0x81, false, true",
                "0x7F, 0x80, false, true"
            )
            fun status(y: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = result)
            }
        }

        @Nested
        inner class Iny16 : Test16BitIndex() {
            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0001, false, false",
                "0xFFFF, 0x0000, true, false",
                "0x8000, 0x8001, false, true",
                "0x7FFF, 0x8000, false, true"
            )
            fun status(y: Int, result: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = result)
            }
        }
    }

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

    @Nested
    inner class Sta : OperationTest(
        "STA",
        { sta }
    ) {
        @Nested
        inner class Sta8 : Test8Bit() {
            @Test
            fun store() {
                prepareProcessor(a = 0x11, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)

                testOperation(a = 0x11, dbr = 0x22)
            }

            @Test
            fun storeOnly1Byte() {
                prepareProcessor(a = 0x9911, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)

                testOperation(a = 0x9911, dbr = 0x22)
            }
        }

        @Nested
        inner class Sta16 : Test16Bit() {
            @Test
            fun store() {
                prepareProcessor(a = 0x9911, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)
                memory.expectWrite(0x22, 0x3345, 0x99)

                testOperation(a = 0x9911, dbr = 0x22)
            }
        }
    }

    @Nested
    inner class Stx : OperationTest(
        "STX",
        { stx }
    ) {
        @Nested
        inner class Sta8 : Test8BitIndex() {
            @Test
            fun store() {
                prepareProcessor(x = 0x11, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)

                testOperation(x = 0x11, dbr = 0x22)
            }
        }

        @Nested
        inner class Stx16 : Test16BitIndex() {
            @Test
            fun store() {
                prepareProcessor(x = 0x9911, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)
                memory.expectWrite(0x22, 0x3345, 0x99)

                testOperation(x = 0x9911, dbr = 0x22)
            }
        }
    }

    @Nested
    inner class Sty : OperationTest(
        "STY",
        { sty }
    ) {
        @Nested
        inner class Sta8 : Test8BitIndex() {
            @Test
            fun store() {
                prepareProcessor(y = 0x11, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)

                testOperation(y = 0x11, dbr = 0x22)
            }
        }

        @Nested
        inner class Sty16 : Test16BitIndex() {
            @Test
            fun store() {
                prepareProcessor(y = 0x9911, dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x11)
                memory.expectWrite(0x22, 0x3345, 0x99)

                testOperation(y = 0x9911, dbr = 0x22)
            }
        }
    }

    @Nested
    inner class Stz : OperationTest(
        "STZ",
        { stz }
    ) {
        @Nested
        inner class Stz8 : Test8Bit() {
            @Test
            fun store() {
                prepareProcessor(dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0x0)

                testOperation(dbr = 0x22)
            }
        }

        @Nested
        inner class Stz16 : Test16Bit() {
            @Test
            fun store() {
                prepareProcessor(dbr = 0x22)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x3344)

                memory.expectWrite(0x22, 0x3344, 0)
                memory.expectWrite(0x22, 0x3345, 0)

                testOperation(dbr = 0x22)
            }
        }
    }
}