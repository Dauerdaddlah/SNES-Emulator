package de.dde.snes.processor

import de.dde.snes.processor.addressmode.AddressModeResult
import de.dde.snes.processor.operation.OperationTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsTest {
    @Test
    fun test_adc() {
        TODO()
    }

    @Test
    fun test_brk() {
        TODO()
    }

    @Test
    fun test_cop() {
        TODO()
    }

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
}