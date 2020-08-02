package de.dde.snes.processor.operation

import de.dde.snes.processor.ProcessorMode
import de.dde.snes.processor.addressmode.AddressModeResult
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
}