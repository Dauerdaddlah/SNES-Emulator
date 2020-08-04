package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.BeforeEach
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

    @Nested
    inner class Tax : OperationTest(
        "TAX",
        { tax }
    ) {
        @Nested
        inner class Tax8 : Test8BitIndex() {
            @Test
            fun transfer_8To8() {
                prepareStatus(a16 = false)
                prepareProcessor(a = 0x1122, x = 0x44)

                testOperation(a = 0x1122, x = 0x22)
            }

            @Test
            fun transfer_16To8() {
                prepareStatus(a16 = true)
                prepareProcessor(a = 0x1122, x = 0x44)

                testOperation(a = 0x1122, x = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(a: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(a16 = false)
                prepareProcessor(a = a)

                status.zero = zero
                status.negative = negative

                testOperation(a = a, x = a)
            }
        }

        @Nested
        inner class Tax16 : Test16BitIndex() {
            @Test
            fun transfer_8To16() {
                prepareStatus(a16 = false)
                prepareProcessor(a = 0x1122, x = 0x3344)

                testOperation(a = 0x1122, x = 0x1122)
            }

            @Test
            fun transfer_16To16() {
                prepareStatus(a16 = true)
                prepareProcessor(a = 0x1122, x = 0x3344)

                testOperation(a = 0x1122, x = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(a: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(a16 = true)
                prepareProcessor(a = a)

                status.zero = zero
                status.negative = negative

                testOperation(a = a, x = a)
            }
        }
    }

    @Nested
    inner class Tay : OperationTest(
        "TAY",
        { tay }
    ) {
        @Nested
        inner class Tax8 : Test8BitIndex() {
            @Test
            fun transfer_8To8() {
                prepareStatus(a16 = false)
                prepareProcessor(a = 0x1122, y = 0x44)

                testOperation(a = 0x1122, y = 0x22)
            }

            @Test
            fun transfer_16To8() {
                prepareStatus(a16 = true)
                prepareProcessor(a = 0x1122, y = 0x44)

                testOperation(a = 0x1122, y = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(a: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(a16 = false)
                prepareProcessor(a = a)

                status.zero = zero
                status.negative = negative

                testOperation(a = a, y = a)
            }
        }

        @Nested
        inner class Tay16 : Test16BitIndex() {
            @Test
            fun transfer_8To16() {
                prepareStatus(a16 = false)
                prepareProcessor(a = 0x1122, y = 0x3344)

                testOperation(a = 0x1122, y = 0x1122)
            }

            @Test
            fun transfer_16To16() {
                prepareStatus(a16 = true)
                prepareProcessor(a = 0x1122, y = 0x3344)

                testOperation(a = 0x1122, y = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(a: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(a16 = true)
                prepareProcessor(a = a)

                status.zero = zero
                status.negative = negative

                testOperation(a = a, y = a)
            }
        }
    }

    @Nested
    inner class Tcd : OperationTest(
        "TCD",
        { tcd }
    ) {
        @Test
        fun transfer_A8() {
            prepareStatus(a16 = false)
            prepareProcessor(a = 0x1122, d = 0x3344)

            testOperation(a = 0x1122, d = 0x1122)
        }
        @Test
        fun transfer_A16() {
            prepareStatus(a16 = true)
            prepareProcessor(a = 0x1122, d = 0x3344)

            testOperation(a = 0x1122, d = 0x1122)
        }

        @ParameterizedTest
        @CsvSource(
            "0x0000, true, false",
            "0x7FFF, false, false",
            "0x8000, false, true",
            "0xFFFF, false, true"
        )
        fun status(a: Int, zero: Boolean, negative: Boolean) {
            prepareStatus(a16 = true)
            prepareProcessor(a = a)

            status.zero = zero
            status.negative = negative

            testOperation(a = a, d = a)
        }
    }

    @Nested
    inner class Tcs : OperationTest(
        "TCS",
        { tcs }
    ) {
        @Nested
        inner class Tcs8 {
            @BeforeEach
            fun initialize() {
                prepareStatus(s16 = false)
            }

            @Test
            fun transfer() {
                prepareStatus(a16 = false)
                prepareProcessor(a = 0x1122, s = 0x0144)

                testOperation(a = 0x1122, s = 0x0122)
            }
        }

        @Nested
        inner class Tcs16 {
            @BeforeEach
            fun initialize() {
                prepareStatus(s16 = true)
            }

            @Test
            fun transfer_A8() {
                prepareStatus(a16 = false)
                prepareProcessor(a = 0x1122, s = 0x3344)

                testOperation(a = 0x1122, s = 0x1122)
            }
            @Test
            fun transfer_A16() {
                prepareStatus(a16 = true)
                prepareProcessor(a = 0x1122, s = 0x3344)

                testOperation(a = 0x1122, s = 0x1122)
            }
        }
    }

    @Nested
    inner class Tdc : OperationTest(
        "TDC",
        { tdc }
    ) {
        @Nested
        inner class Tdc8 : Test8Bit() {
            @Test
            fun transfer() {
                prepareProcessor(d = 0x22, a = 0x44)

                testOperation(d = 0x22, a = 0x22)
            }

            @Test
            fun transferAlways16() {
                prepareProcessor(d = 0x1122, a = 0x3344)

                testOperation(d = 0x1122, a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(d: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(d = d)

                status.zero = zero
                status.negative = negative

                testOperation(d = d, a = d)
            }
        }

        @Nested
        inner class Tdc16 : Test16Bit() {
            @Test
            fun transfer() {
                prepareProcessor(d = 0x1122, a = 0x3344)

                testOperation(d = 0x1122, a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(d: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(d = d)

                status.zero = zero
                status.negative = negative

                testOperation(d = d, a = d)
            }
        }
    }

    @Nested
    inner class Tsc : OperationTest(
        "TSC",
        { tsc }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Tsc8 : Test8Bit() {
            @Test
            fun transferAlways16() {
                prepareProcessor(s = 0x1122, a = 0x3344)

                testOperation(s = 0x1122, a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(s: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(s = s)

                status.zero = zero
                status.negative = negative

                testOperation(s = s, a = s)
            }
        }

        @Nested
        inner class Tsc16 : Test16Bit() {
            @Test
            fun transfer() {
                prepareProcessor(s = 0x1122, a = 0x3344)

                testOperation(s = 0x1122, a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(s: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(s = s)

                status.zero = zero
                status.negative = negative

                testOperation(s = s, a = s)
            }
        }
    }

    @Nested
    inner class Tsx : OperationTest(
        "TSX",
        { tsx }
    ) {
        @Nested
        inner class Tsx8 : Test8BitIndex() {
            @Test
            fun transfer_8To8() {
                prepareStatus(s16 = false)
                prepareProcessor(s = 0x0122, x = 0x44)

                testOperation(s = 0x0122, x = 0x22)
            }

            @Test
            fun transfer_16To8() {
                prepareStatus(s16 = true)
                prepareProcessor(s = 0x1122, x = 0x44)

                testOperation(s = 0x1122, x = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(s: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(s16 = false)
                prepareProcessor(s = s)

                status.zero = zero
                status.negative = negative

                testOperation(s = 0x0100 or s, x = s)
            }
        }

        @Nested
        inner class Tsx16 : Test16BitIndex() {
            @Test
            fun transfer_16To16() {
                prepareStatus(s16 = true)
                prepareProcessor(s = 0x1122, x = 0x3344)

                testOperation(s = 0x1122, x = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(s: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(s16 = true)
                prepareProcessor(s = s)

                status.zero = zero
                status.negative = negative

                testOperation(s = s, x = s)
            }
        }
    }

    @Nested
    inner class Txa : OperationTest(
        "TXA",
        { txa }
    ) {
        @Nested
        inner class Txa8 : Test8Bit() {
            @Test
            fun transfer_8To8() {
                prepareStatus(i16 = false)
                prepareProcessor(x = 0x22, a = 0x44)

                testOperation(x = 0x22, a = 0x22)
            }

            @Test
            fun transfer_16To8() {
                prepareStatus(i16 = true)
                prepareProcessor(x = 0x1122, a = 0x44)

                testOperation(x = 0x1122, a = 0x22)
            }

            @Test
            fun transfer_onlyLowByte() {
                prepareStatus(i16 = true)
                prepareProcessor(x = 0x1122, a = 0x3344)

                testOperation(x = 0x1122, a = 0x3322)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(x: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(i16 = false)
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = x, a = x)
            }
        }

        @Nested
        inner class Txa16 : Test16Bit() {
            @Test
            fun transfer_8To16() {
                prepareStatus(i16 = false)
                prepareProcessor(x = 0x22, a = 0x3344)

                testOperation(x = 0x22, a = 0x0022)
            }

            @Test
            fun transfer_16To16() {
                prepareStatus(i16 = true)
                prepareProcessor(x = 0x1122, a = 0x3344)

                testOperation(x = 0x1122, a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(x: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(i16 = true)
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = x, a = x)
            }
        }
    }

    @Nested
    inner class Txs : OperationTest(
        "TXS",
        { txs }
    ) {
        @Nested
        inner class Txs8 {
            @BeforeEach
            fun initialize() {
                prepareStatus(s16 = false)
            }

            @Test
            fun transfer() {
                prepareStatus(i16 = false)
                prepareProcessor(x = 0x22, s = 0x0144)

                testOperation(x = 0x22, s = 0x0122)
            }
        }

        @Nested
        inner class Txs16 {
            @BeforeEach
            fun initialize() {
                prepareStatus(s16 = true)
            }

            @Test
            fun transfer_8To16() {
                prepareStatus(i16 = false)
                prepareProcessor(x = 0x22, s = 0x3344)

                testOperation(x = 0x22, s = 0x0022)
            }

            @Test
            fun transfer_16To16() {
                prepareStatus(i16 = true)
                prepareProcessor(x = 0x1122, s = 0x3344)

                testOperation(x = 0x1122, s = 0x1122)
            }
        }
    }

    @Nested
    inner class Txy : OperationTest(
        "TXY",
        { txy }
    ) {
        @Nested
        inner class Txy8 : Test8BitIndex() {
            @Test
            fun transfer() {
                prepareProcessor(x = 0x22, y = 0x44)

                testOperation(x = 0x22, y = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(x: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = x, y = x)
            }
        }

        @Nested
        inner class Txy16 : Test16BitIndex() {
            @Test
            fun transfer() {
                prepareProcessor(x = 0x1122, y = 0x3344)

                testOperation(x = 0x1122, y = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(x: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(x = x)

                status.zero = zero
                status.negative = negative

                testOperation(x = x, y = x)
            }
        }
    }

    @Nested
    inner class Tya : OperationTest(
        "TYA",
        { tya }
    ) {
        @Nested
        inner class Tya8 : Test8Bit() {
            @Test
            fun transfer_8To8() {
                prepareStatus(i16 = false)
                prepareProcessor(y = 0x22, a = 0x44)

                testOperation(y = 0x22, a = 0x22)
            }

            @Test
            fun transfer_16To8() {
                prepareStatus(i16 = true)
                prepareProcessor(y = 0x1122, a = 0x44)

                testOperation(y = 0x1122, a = 0x22)
            }

            @Test
            fun transfer_onlyLowByte() {
                prepareStatus(i16 = true)
                prepareProcessor(y = 0x1122, a = 0x3344)

                testOperation(y = 0x1122, a = 0x3322)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(y: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(i16 = false)
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = y, a = y)
            }
        }

        @Nested
        inner class Tya16 : Test16Bit() {
            @Test
            fun transfer_8To16() {
                prepareStatus(i16 = false)
                prepareProcessor(y = 0x22, a = 0x3344)

                testOperation(y = 0x22, a = 0x0022)
            }

            @Test
            fun transfer_16To16() {
                prepareStatus(i16 = true)
                prepareProcessor(y = 0x1122, a = 0x3344)

                testOperation(y = 0x1122, a = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(y: Int, zero: Boolean, negative: Boolean) {
                prepareStatus(i16 = true)
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = y, a = y)
            }
        }
    }

    @Nested
    inner class Tyx : OperationTest(
        "TYX",
        { tyx }
    ) {
        @Nested
        inner class Tyx8 : Test8BitIndex() {
            @Test
            fun transfer() {
                prepareProcessor(y = 0x22, x = 0x44)

                testOperation(y = 0x22, x = 0x22)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, true, false",
                "0x7F, false, false",
                "0x80, false, true",
                "0xFF, false, true"
            )
            fun status(y: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = y, x = y)
            }
        }

        @Nested
        inner class Tyx16 : Test16BitIndex() {
            @Test
            fun transfer() {
                prepareProcessor(y = 0x1122, x = 0x3344)

                testOperation(y = 0x1122, x = 0x1122)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, true, false",
                "0x7FFF, false, false",
                "0x8000, false, true",
                "0xFFFF, false, true"
            )
            fun status(y: Int, zero: Boolean, negative: Boolean) {
                prepareProcessor(y = y)

                status.zero = zero
                status.negative = negative

                testOperation(y = y, x = y)
            }
        }
    }

    @Nested
    inner class Xba : OperationTest(
        "XBA",
        { xba }
    ) {
        @Nested
        inner class Xba8 : Test8Bit() {
            @Test
            fun transfer() {
                prepareProcessor(a = 0x1122)

                testOperation(a = 0x2211)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true",
                "0x0011, 0x1100, false, true",
                "0x8011, 0x1180, true, false"
            )
            fun status(a1: Int, a2: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor(a = a1)

                status.zero = zero
                status.negative = negative

                testOperation(a = a2)
            }
        }

        @Nested
        inner class Xba16 : Test16Bit() {
            @Test
            fun transfer() {
                prepareProcessor(a = 0x1122)

                testOperation(a = 0x2211)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true",
                "0x0011, 0x1100, false, true",
                "0x8011, 0x1180, true, false",
                "0x1234, 0x3412, false, false"
            )
            fun status(a1: Int, a2: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor(a = a1)

                status.zero = zero
                status.negative = negative

                testOperation(a = a2)
            }
        }
    }
}