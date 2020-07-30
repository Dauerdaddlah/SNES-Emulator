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

    // TODO test for overflow bit as well
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
}