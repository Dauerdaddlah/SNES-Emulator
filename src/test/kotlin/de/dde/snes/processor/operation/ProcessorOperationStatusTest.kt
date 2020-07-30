package de.dde.snes.processor.operation

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
}