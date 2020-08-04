package de.dde.snes.processor.operation

import de.dde.snes.processor.ProcessorMode
import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationStatusTest {
    @Nested
    inner class Clc : OperationTest(
        "CLC",
        { clc }
    ) {
        @Test
        fun clear() {
            status.carry = true
            prepareProcessor()

            status.carry = false
            testOperation()
        }

        @Test
        fun doNothing() {
            status.carry = false
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Cld : OperationTest(
        "CLD",
        { cld }
    ) {
        @Test
        fun clear() {
            status.decimal = true
            prepareProcessor()

            status.decimal = false
            testOperation()
        }

        @Test
        fun doNothing() {
            status.decimal = false
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Cli : OperationTest(
        "CLI",
        { cli }
    ) {
        @Test
        fun clear() {
            status.irqDisable = true
            prepareProcessor()

            status.irqDisable = false
            testOperation()
        }

        @Test
        fun doNothing() {
            status.irqDisable = false
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Clv : OperationTest(
        "CLV",
        { clv }
    ) {
        @Test
        fun clear() {
            status.overflow = true
            prepareProcessor()

            status.overflow = false
            testOperation()
        }

        @Test
        fun doNothing() {
            status.overflow = false
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Rep : OperationTest(
        "REP",
        { rep }
    ) {
        @ParameterizedTest
        @CsvSource(
            "0, 0, 0",
            "0xFF, 0xFF, 0x00",
            "0xFF, 0xF0, 0x0F",
            "0xFF, 0x0F, 0xF0",
            "0xF0, 0x0F, 0xF0",
            "0x3F, 0xF0, 0x0F"
        )
        fun native(start: Int, fetch: Int, result: Int) {
            mode = ProcessorMode.NATIVE
            status.emulationMode = false
            status.set(start)
            prepareProcessor()

            addressMode.result = AddressModeResult.IMMEDIATE
            addressMode.fetchNext(fetch)

            status.set(result)

            testOperation()
        }

        @ParameterizedTest
        @CsvSource(
            "0x30, 0x00, 0x30",
            "0xFF, 0xFF, 0x30",
            "0xFF, 0xF0, 0x3F",
            "0xFF, 0x0F, 0xF0",
            "0xF0, 0x0F, 0xF0",
            "0x3F, 0xF0, 0x3F"
        )
        fun emulation(start: Int, fetch: Int, result: Int) {
            // 0x30 is always set in emulation mode
            mode = ProcessorMode.EMULATION
            status.emulationMode = true
            status.set(start)
            prepareProcessor()

            addressMode.result = AddressModeResult.IMMEDIATE
            addressMode.fetchNext(fetch)

            status.set(result)

            testOperation()
        }
    }

    @Nested
    inner class sep : OperationTest(
        "SEP",
        { sep }
    ) {
        @ParameterizedTest
        @CsvSource(
            "0, 0, 0",
            "0x00, 0xFF, 0xFF",
            "0x0F, 0xF0, 0xFF",
            "0xF0, 0xFF, 0xFF",
            "0x3F, 0xF0, 0xFF"
        )
        fun native(start: Int, fetch: Int, result: Int) {
            mode = ProcessorMode.NATIVE
            status.emulationMode = false
            status.set(start)
            prepareProcessor()

            addressMode.result = AddressModeResult.IMMEDIATE
            addressMode.fetchNext(fetch)

            status.set(result)

            testOperation()
        }

        @ParameterizedTest
        @CsvSource(
            "0x30, 0x00, 0x30",
            "0xFF, 0xFF, 0xFF",
            "0x30, 0x0F, 0x3F",
            "0x30, 0xF0, 0xF0",
            "0xF0, 0x0F, 0xFF"
        )
        fun emulation(start: Int, fetch: Int, result: Int) {
            // 0x30 is always set in emulation mode
            mode = ProcessorMode.EMULATION
            status.emulationMode = true
            status.set(start)
            prepareProcessor()

            addressMode.result = AddressModeResult.IMMEDIATE
            addressMode.fetchNext(fetch)

            status.set(result)

            testOperation()
        }
    }

    @Nested
    inner class Sec : OperationTest(
        "SEC",
        { sec }
    ) {
        @Test
        fun set() {
            status.carry = false
            prepareProcessor()

            status.carry = true
            testOperation()
        }

        @Test
        fun doNothing() {
            status.carry = true
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Sed : OperationTest(
        "SED",
        { sed }
    ) {
        @Test
        fun clear() {
            status.decimal = false
            prepareProcessor()

            status.decimal = true
            testOperation()
        }

        @Test
        fun doNothing() {
            status.decimal = true
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Sei : OperationTest(
        "SEI",
        { sei }
    ) {
        @Test
        fun clear() {
            status.irqDisable = false
            prepareProcessor()

            status.irqDisable = true
            testOperation()
        }

        @Test
        fun doNothing() {
            status.irqDisable = true
            prepareProcessor()

            testOperation()
        }
    }

    @Nested
    inner class Trb : OperationTest(
        "TRB",
        { trb }
    ) {
        @Nested
        inner class Trb8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(a = 0x0F, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, 0xFF)
                memory.expectWrite(0x33, 0x1122, 0xF0)

                testOperation(a = 0x0F, dbr = 0x33)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, 0x00, 0x00, true",
                "0xFF, 0xFF, 0x00, false",
                "0x00, 0xFF, 0xFF, true",
                "0xAA, 0x55, 0x55, true",
                "0xF0, 0xFF, 0x0F, false"
            )
            fun status(a: Int, fetch: Int, write: Int, zero: Boolean) {
                prepareProcessor(a = a, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, fetch)
                memory.expectWrite(0x33, 0x1122, write)

                status.zero = zero

                testOperation(a = a, dbr = 0x33)
            }
        }

        @Nested
        inner class Trb16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(a = 0x0F0F, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, 0xFF)
                memory.returnFor(0x33, 0x1123, 0xFF)
                memory.expectWrite(0x33, 0x1122, 0xF0)
                memory.expectWrite(0x33, 0x1123, 0xF0)

                testOperation(a = 0x0F0F, dbr = 0x33)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x00, 0x00, 0x00, 0x00, true",
                "0xFFFF, 0xFF, 0xFF, 0x00, 0x00, false",
                "0x0000, 0xFF, 0xFF, 0xFF, 0xFF, true",
                "0xAAAA, 0x55, 0x55, 0x55, 0x55, true",
                "0xF00F, 0xFF, 0xFF, 0xF0, 0x0F, false"
            )
            fun status(a: Int, fetch1: Int, fetch2: Int, write1: Int, write2: Int, zero: Boolean) {
                prepareProcessor(a = a, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, fetch1)
                memory.returnFor(0x33, 0x1123, fetch2)
                memory.expectWrite(0x33, 0x1122, write1)
                memory.expectWrite(0x33, 0x1123, write2)

                status.zero = zero

                testOperation(a = a, dbr = 0x33)
            }
        }
    }

    @Nested
    inner class Tsb : OperationTest(
        "TSB",
        { tsb }
    ) {
        @Nested
        inner class Trb8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(a = 0x0F, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, 0xF1)
                memory.expectWrite(0x33, 0x1122, 0xFF)

                testOperation(a = 0x0F, dbr = 0x33)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, 0x00, 0x00, true",
                "0xFF, 0x00, 0xFF, true",
                "0x10, 0xFF, 0xFF, false",
                "0xAA, 0x55, 0xFF, true",
                "0xF0, 0x31, 0xF1, false"
            )
            fun status(a: Int, fetch: Int, write: Int, zero: Boolean) {
                prepareProcessor(a = a, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, fetch)
                memory.expectWrite(0x33, 0x1122, write)

                status.zero = zero

                testOperation(a = a, dbr = 0x33)
            }
        }

        @Nested
        inner class Trb16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(a = 0x0F0F, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, 0xF1)
                memory.returnFor(0x33, 0x1123, 0xF0)
                memory.expectWrite(0x33, 0x1122, 0xFF)
                memory.expectWrite(0x33, 0x1123, 0xFF)

                testOperation(a = 0x0F0F, dbr = 0x33)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x00, 0x00, 0x00, 0x00, true",
                "0xFFFF, 0xFF, 0xFF, 0xFF, 0xFF, false",
                "0x0000, 0xFF, 0xFF, 0xFF, 0xFF, true",
                "0xAAAA, 0x55, 0x55, 0xFF, 0xFF, true",
                "0xF00F, 0xF3, 0x3F, 0xFF, 0xFF, false"
            )
            fun status(a: Int, fetch1: Int, fetch2: Int, write1: Int, write2: Int, zero: Boolean) {
                prepareProcessor(a = a, dbr = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1122)

                memory.returnFor(0x33, 0x1122, fetch1)
                memory.returnFor(0x33, 0x1123, fetch2)
                memory.expectWrite(0x33, 0x1122, write1)
                memory.expectWrite(0x33, 0x1123, write2)

                status.zero = zero

                testOperation(a = a, dbr = 0x33)
            }
        }
    }
}