package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsBitTest {
    @Nested
    inner class And : OperationTest(
        "AND",
        { and }
    ) {
        @Nested
        inner class And8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x11, a = 0x0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x22)

                memory.returnFor(0x11, 0x22, 0x3C)

                testOperation(dbr = 0x11, a = 0x0C)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0xF0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)

                testOperation(a = 0x30)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true",
                "0x80, 0x80, 0x80, true, false",
                "0xFF, 0xFF, 0xFF, true, false",
                "0xAA, 0x55, 0x00, false, true",
                "0x3C, 0xF0, 0x30, false, false",
                "0x0F, 0x3C, 0x0C, false, false"
            )
            fun status(a: Int, fetch: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch)

                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }
        }

        @Nested
        inner class And16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0xF00F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0x3C)
                memory.returnFor(0x12, 0x2333, 0x43)

                testOperation(dbr = 0x12, a = 0x400C)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0xF0F0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)
                addressMode.fetchNext(0x3C)

                testOperation(a = 0x3030)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, 0, false, true",
                "0x8000, 0x00, 0x80, 0x8000, true, false",
                "0xFFFF, 0xFF, 0xFF, 0xFFFF, true, false",
                "0xAAAA, 0x55, 0x55, 0x0000, false, true",
                "0xC33C, 0xF0, 0x0F, 0x0330, false, false",
                "0x0FF0, 0xC3, 0x3C, 0x0CC0, false, false"
            )
            fun status(a: Int, fetch1: Int, fetch2: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }
        }
    }

    @Nested
    inner class Bit : OperationTest(
        "BIT",
        { bit }
    ) {
        @Nested
        inner class Bit8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x11, a = 0x0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x22)

                memory.returnFor(0x11, 0x22, 0x3C)

                testOperation(dbr = 0x11, a = 0x0F)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0xF0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)

                testOperation(a = 0xF0)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true, false",
                "0x80, 0x80, true, false, false",
                "0x40, 0x40, false, false, true",
                "0xFF, 0xFF, true, false, true",
                "0xAA, 0x55, false, true, false",
                "0x3C, 0xF0, false, false, false",
                "0x0F, 0x3C, false, false, false",
                "0xFF, 0xF0, true, false, true"
            )
            fun status(a: Int, fetch: Int, negative: Boolean, zero: Boolean, overflow: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, fetch)

                status.negative = negative
                status.zero = zero
                status.overflow = overflow

                testOperation(a = a)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, true",
                "0x80, 0x80, false",
                "0x40, 0x40, false",
                "0xFF, 0xFF, false",
                "0xAA, 0x55, true",
                "0x3C, 0xF0, false",
                "0x0F, 0x3C, false",
                "0xFF, 0xF0, false"
            )
            fun status_immediate(a: Int, fetch: Int, zero: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch)

                status.zero = zero

                testOperation(a = a)
            }
        }

        @Nested
        inner class Bit16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0xF00F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0x3C)
                memory.returnFor(0x12, 0x2333, 0x33)

                testOperation(dbr = 0x12, a = 0xF00F)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0xF0F0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)
                addressMode.fetchNext(0x3C)

                testOperation(a = 0xF0F0)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true, false",
                "0x8000, 0x00, 0x80, true, false, false",
                "0x4000, 0x00, 0x40, false, false, true",
                "0xFFFF, 0xFF, 0xFF, true, false, true",
                "0xAAAA, 0x55, 0x55, false, true, false",
                "0xC33C, 0xF0, 0x0F, false, false, false",
                "0x0FF0, 0xC3, 0x3C, false, false, false",
                "0xC000, 0x00, 0xC0, true, false, true"
            )
            fun status(a: Int, fetch1: Int, fetch2: Int, negative: Boolean, zero: Boolean, overflow: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1111)

                memory.returnFor(0x00, 0x1111, fetch1)
                memory.returnFor(0x00, 0x1112, fetch2)

                status.negative = negative
                status.zero = zero
                status.overflow = overflow

                testOperation(a = a)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, true",
                "0x8000, 0x00, 0x80, false",
                "0x4000, 0x00, 0x40, false",
                "0xFFFF, 0xFF, 0xFF, false",
                "0xAAAA, 0x55, 0x55, true",
                "0xC33C, 0xF0, 0x0F, false",
                "0x0FF0, 0xC3, 0x3C, false",
                "0xC000, 0x00, 0xC0, false"
            )
            fun status_immediate(a: Int, fetch1: Int, fetch2: Int, zero: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.zero = zero

                testOperation(a = a)
            }
        }
    }

    @Nested
    inner class Cmp : OperationTest(
        "CMP",
        { cmp }
    ) {
        @Nested
        inner class Cmp8 : Test8Bit() {
            @Test
            fun immediate() {
                prepareProcessor(a = 0x70)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)

                testOperation(a = 0x70)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01, a = 0x0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x01, 0x2332, 0x06)

                testOperation(dbr = 0x01, a = 0x0F)
            }

            @Test
            fun ignoreHighByte() {
                prepareProcessor(a = 0xF070)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)

                testOperation(a = 0xF070)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true, false",
                "0xFF, 0xFF, false, true, false",
                "0x80, 0x00, true, false, false",
                "0x01, 0xFF, false, false, true", // 74 35
                "0x00, 0x01, true, false, true"
            )
            fun status(a: Int, fetch: Int, negative: Boolean, zero: Boolean, carry: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch)

                status.negative = negative
                status.zero = zero
                status.carry = carry

                testOperation(a = a)
            }
        }

        @Nested
        inner class Cmp16 : Test16Bit() {
            @Test
            fun immediate() {
                prepareProcessor(a = 0x7070)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)
                addressMode.fetchNext(0x3C)

                testOperation(a = 0x7070)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01, a = 0x0F0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x01, 0x2332, 0x06)
                memory.returnFor(0x01, 0x2333, 0x06)

                testOperation(dbr = 0x01, a = 0x0F0F)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true, false",
                "0xFFFF, 0xFF, 0xFF, false, true, false",
                "0x8000, 0x00, 0x00, true, false, false",
                "0x0001, 0xFF, 0xFF, false, false, true",
                "0x0000, 0x01, 0x00, true, false, true"
            )
            fun status(a: Int, fetch1: Int, fetch2: Int, negative: Boolean, zero: Boolean, carry: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.negative = negative
                status.zero = zero
                status.carry = carry

                testOperation(a = a)
            }
        }
    }

    @Nested
    inner class Cpx : OperationTest(
        "CPX",
        { cpx }
    ) {
        @Nested
        inner class Cpx8 : Test8BitIndex() {
            @Test
            fun immediate() {
                prepareProcessor(x = 0x70)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)

                testOperation(x = 0x70)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01, x = 0x0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x01, 0x2332, 0x06)

                testOperation(dbr = 0x01, x = 0x0F)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true, false",
                "0xFF, 0xFF, false, true, false",
                "0x80, 0x00, true, false, false",
                "0x01, 0xFF, false, false, true",
                "0x00, 0x01, true, false, true"
            )
            fun status(x: Int, fetch: Int, negative: Boolean, zero: Boolean, carry: Boolean) {
                prepareProcessor(x = x)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch)

                status.negative = negative
                status.zero = zero
                status.carry = carry

                testOperation(x = x)
            }
        }

        @Nested
        inner class Cpx16 : Test16BitIndex() {
            @Test
            fun immediate() {
                prepareProcessor(x = 0x7070)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)
                addressMode.fetchNext(0x3C)

                testOperation(x = 0x7070)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01, x = 0x0F0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x01, 0x2332, 0x06)
                memory.returnFor(0x01, 0x2333, 0x06)

                testOperation(dbr = 0x01, x = 0x0F0F)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true, false",
                "0xFFFF, 0xFF, 0xFF, false, true, false",
                "0x8000, 0x00, 0x00, true, false, false",
                "0x0001, 0xFF, 0xFF, false, false, true",
                "0x0000, 0x01, 0x00, true, false, true"
            )
            fun status(x: Int, fetch1: Int, fetch2: Int, negative: Boolean, zero: Boolean, carry: Boolean) {
                prepareProcessor(x = x)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.negative = negative
                status.zero = zero
                status.carry = carry

                testOperation(x = x)
            }
        }
    }

    @Nested
    inner class Cpy : OperationTest(
        "CPY",
        { cpy }
    ) {
        @Nested
        inner class Cpy8 : Test8BitIndex() {
            @Test
            fun immediate() {
                prepareProcessor(y = 0x70)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)

                testOperation(y = 0x70)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01, y = 0x0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x01, 0x2332, 0x06)

                testOperation(dbr = 0x01, y = 0x0F)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true, false",
                "0xFF, 0xFF, false, true, false",
                "0x80, 0x00, true, false, false",
                "0x01, 0xFF, false, false, true",
                "0x00, 0x01, true, false, true"
            )
            fun status(y: Int, fetch: Int, negative: Boolean, zero: Boolean, carry: Boolean) {
                prepareProcessor(y = y)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch)

                status.negative = negative
                status.zero = zero
                status.carry = carry

                testOperation(y = y)
            }
        }

        @Nested
        inner class Cpy16 : Test16BitIndex() {
            @Test
            fun immediate() {
                prepareProcessor(y = 0x7070)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x3C)
                addressMode.fetchNext(0x3C)

                testOperation(y = 0x7070)
            }

            @Test
            fun address() {
                prepareProcessor(dbr = 0x01, y = 0x0F0F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x01, 0x2332, 0x06)
                memory.returnFor(0x01, 0x2333, 0x06)

                testOperation(dbr = 0x01, y = 0x0F0F)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true, false",
                "0xFFFF, 0xFF, 0xFF, false, true, false",
                "0x8000, 0x00, 0x00, true, false, false",
                "0x0001, 0xFF, 0xFF, false, false, true",
                "0x0000, 0x01, 0x00, true, false, true"
            )
            fun status(y: Int, fetch1: Int, fetch2: Int, negative: Boolean, zero: Boolean, carry: Boolean) {
                prepareProcessor(y = y)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.negative = negative
                status.zero = zero
                status.carry = carry

                testOperation(y = y)
            }
        }
    }

    @Nested
    inner class Eor : OperationTest(
        "EOR",
        { eor }
    ) {
        @Nested
        inner class Eor8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x11, a = 0xFF)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x22)

                memory.returnFor(0x11, 0x22, 0xFC)

                testOperation(dbr = 0x11, a = 0x03)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0xFF)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0xFC)

                testOperation(a = 0x03)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true",
                "0x80, 0x00, 0x80, true, false",
                "0xFF, 0xFF, 0x00, false, true",
                "0xAA, 0x55, 0xFF, true, false",
                "0x3C, 0xF0, 0xCC, true, false",
                "0x0F, 0x3C, 0x33, false, false"
            )
            fun status(a: Int, fetch: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch)

                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }
        }

        @Nested
        inner class Eor16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0xF00F)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0xFF)
                memory.returnFor(0x12, 0x2333, 0xFF)

                testOperation(dbr = 0x12, a = 0x0FF0)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0xF0F0)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0xFF)
                addressMode.fetchNext(0xFF)

                testOperation(a = 0x0F0F)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, 0, false, true",
                "0x8000, 0x00, 0x00, 0x8000, true, false",
                "0xFFFF, 0xFF, 0xFF, 0x0000, false, true",
                "0xAAAA, 0x55, 0x55, 0xFFFF, true, false",
                "0xC33C, 0x0F, 0xF0, 0x3333, false, false",
                "0x0FF0, 0x3C, 0xC3, 0xCCCC, true, false"
            )
            fun status(a: Int, fetch1: Int, fetch2: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(fetch1)
                addressMode.fetchNext(fetch2)

                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }
        }
    }
}