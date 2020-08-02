package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsStackTest {
    @Nested
    inner class Pea : OperationTest(
        "PEA",
        { pea }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            prepareProcessor(dbr = 0x10, s = 0xFFFF)

            addressMode.result = AddressModeResult.ADDRESS_DBR
            addressMode.fetchNext(0x1234)

            memory.expectWrite(0x00, 0xFFFF, 0x12)
            memory.expectWrite(0x00, 0xFFFE, 0x34)

            testOperation(dbr = 0x10, s = 0xFFFD)
        }
    }

    @Nested
    inner class Pei : OperationTest(
        "PEI",
        { pei }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            prepareProcessor(s = 0xFFFF)

            addressMode.result = AddressModeResult.ADDRESS_0
            addressMode.fetchNext(0x1234)

            memory.expectWrite(0x00, 0xFFFF, 0x12)
            memory.expectWrite(0x00, 0xFFFE, 0x34)

            testOperation(s = 0xFFFD)
        }
    }

    @Nested
    inner class Per : OperationTest(
        "PER",
        { per }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            prepareProcessor(pbr = 0x10, s = 0xFFFF)

            addressMode.result = AddressModeResult.SHORTADDRESS
            addressMode.fetchNext(0x1234)

            memory.expectWrite(0x00, 0xFFFF, 0x12)
            memory.expectWrite(0x00, 0xFFFE, 0x34)

            testOperation(pbr = 0x10, s = 0xFFFD)
        }
    }

    @Nested
    inner class Pha : OperationTest(
        "PHA",
        { pha }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Pha8 : Test8Bit() {
            @Test
            fun push() {
                prepareProcessor(a = 0x11, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x11)

                testOperation(a = 0x11, s = 0xFFFE)
            }

            @Test
            fun pushOnly1Byte() {
                prepareProcessor(a = 0x2211, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x11)

                testOperation(a = 0x2211, s = 0xFFFE)
            }
        }

        @Nested
        inner class Pha16 : Test16Bit() {
            @Test
            fun push() {
                prepareProcessor(a = 0x2211, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x22)
                memory.expectWrite(0x00, 0xFFFE, 0x11)

                testOperation(a = 0x2211, s = 0xFFFD)
            }
        }
    }

    @Nested
    inner class Phb : OperationTest(
        "PHB",
        { phb }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            prepareProcessor(dbr = 0x11, s = 0xFFFF)

            memory.expectWrite(0x00, 0xFFFF, 0x11)

            testOperation(dbr = 0x11, s = 0xFFFE)
        }
    }

    @Nested
    inner class Phd : OperationTest(
        "PHD",
        { phd }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            prepareProcessor(d = 0x2211, s = 0xFFFF)

            memory.expectWrite(0x00, 0xFFFF, 0x22)
            memory.expectWrite(0x00, 0xFFFE, 0x11)

            testOperation(d = 0x2211, s = 0xFFFD)
        }
    }

    @Nested
    inner class Phk : OperationTest(
        "PHK",
        { phk }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            prepareProcessor(pbr = 0x11, s = 0xFFFF)

            memory.expectWrite(0x00, 0xFFFF, 0x11)

            testOperation(pbr = 0x11, s = 0xFFFE)
        }
    }

    @Nested
    inner class Php : OperationTest(
        "PHP",
        { php }
    ) {
        @Test
        fun push() {
            prepareStatus(s16 = true)
            status.carry = true
            status.overflow = true
            status.memory = true
            prepareProcessor(s = 0xFFFF)

            memory.expectWrite(0x00, 0xFFFF, status.get())

            testOperation(s = 0xFFFE)
        }
    }

    @Nested
    inner class Phx : OperationTest(
        "PHX",
        { phx }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Phx8 : Test8BitIndex() {
            @Test
            fun push() {
                prepareProcessor(x = 0x11, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x11)

                testOperation(x = 0x11, s = 0xFFFE)
            }
        }

        @Nested
        inner class Phx16 : Test16BitIndex() {
            @Test
            fun push() {
                prepareProcessor(x = 0x2211, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x22)
                memory.expectWrite(0x00, 0xFFFE, 0x11)

                testOperation(x = 0x2211, s = 0xFFFD)
            }
        }
    }

    @Nested
    inner class Phy : OperationTest(
        "PHY",
        { phy }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Phx8 : Test8BitIndex() {
            @Test
            fun push() {
                prepareProcessor(y = 0x11, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x11)

                testOperation(y = 0x11, s = 0xFFFE)
            }
        }

        @Nested
        inner class Phx16 : Test16BitIndex() {
            @Test
            fun push() {
                prepareProcessor(y = 0x2211, s = 0xFFFF)

                memory.expectWrite(0x00, 0xFFFF, 0x22)
                memory.expectWrite(0x00, 0xFFFE, 0x11)

                testOperation(y = 0x2211, s = 0xFFFD)
            }
        }
    }

    @Nested
    inner class Pla : OperationTest(
        "PLA",
        { pla }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Pla8 : Test8Bit() {
            @Test
            fun pull() {
                prepareProcessor(a = 0x11, s = 0xFFFF)

                memory.returnFor(0x00, 0x0000, 0x22)

                testOperation(a = 0x22, s = 0x0000)
            }

            @ParameterizedTest
            @CsvSource(
                "0, false, true",
                "0x80, true, false",
                "0xFF, true, false",
                "0x7F, false, false"
            )
            fun status(fetch: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                memory.returnFor(0x00, 0x0001, fetch)

                status.negative = negative
                status.zero = zero

                testOperation(a = fetch, s = 0x0001)
            }
        }

        @Nested
        inner class Pla16 : Test16Bit() {
            @Test
            fun pull() {
                prepareProcessor(a = 0x1122)

                memory.returnFor(0x00, 0x0001, 0x44)
                memory.returnFor(0x00, 0x0002, 0x33)

                testOperation(a = 0x3344, s = 0x0002)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true",
                "0x00, 0x80, 0x8000, true, false",
                "0xFF, 0xFF, 0xFFFF, true, false",
                "0xFF, 0x7F, 0x7FFF, false, false"
            )
            fun status(fetch1: Int, fetch2: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                memory.returnFor(0x00, 0x0001, fetch1)
                memory.returnFor(0x00, 0x0002, fetch2)

                status.negative = negative
                status.zero = zero

                testOperation(a = result, s = 0x0002)
            }
        }
    }

    @Nested
    inner class Plb : OperationTest(
        "PLB",
        { plb }
    ) {
        @Test
        fun pull() {
            prepareStatus(s16 = true)
            prepareProcessor(dbr = 0x11)

            memory.returnFor(0x00, 0x0001, 0x22)

            testOperation(dbr = 0x22, s = 0x0001)
        }

        @ParameterizedTest
        @CsvSource(
            "0, false, true",
            "0x80, true, false",
            "0xFF, true, false",
            "0x7F, false, false"
        )
        fun status(fetch: Int, negative: Boolean, zero: Boolean) {
            prepareStatus(s16 = true)
            prepareProcessor()

            memory.returnFor(0x00, 0x0001, fetch)

            status.negative = negative
            status.zero = zero

            testOperation(dbr = fetch, s = 0x0001)
        }
    }

    @Nested
    inner class Pld : OperationTest(
        "PLD",
        { pld }
    ) {
        @Test
        fun pull() {
            prepareStatus(s16 = true)
            prepareProcessor(d = 0x1122)

            memory.returnFor(0x00, 0x0001, 0x44)
            memory.returnFor(0x00, 0x0002, 0x33)

            testOperation(d = 0x3344, s = 0x0002)
        }

        @ParameterizedTest
        @CsvSource(
            "0, 0, 0, false, true",
            "0x00, 0x80, 0x8000, true, false",
            "0xFF, 0xFF, 0xFFFF, true, false",
            "0xFF, 0x7F, 0x7FFF, false, false"
        )
        fun status(fetch1: Int, fetch2: Int, result: Int, negative: Boolean, zero: Boolean) {
            prepareStatus(s16 = true)
            prepareProcessor()

            memory.returnFor(0x00, 0x0001, fetch1)
            memory.returnFor(0x00, 0x0002, fetch2)

            status.negative = negative
            status.zero = zero

            testOperation(d = result, s = 0x0002)
        }
    }

    @Nested
    inner class Plp : OperationTest(
        "PLP",
        { plp }
    ) {
        @Test
        fun pull_0() {
            prepareStatus(s16 = true)
            status.set(0xFF)
            prepareProcessor()

            memory.returnFor(0, 1, 0)

            status.set(0)

            testOperation(s = 0x0001)
        }

        @Test
        fun pull_FF() {
            prepareStatus(s16 = true)
            status.set(0x0)
            prepareProcessor()

            memory.returnFor(0, 1, 0xFF)

            status.set(0xFF)

            testOperation(s = 0x0001)
        }
    }

    @Nested
    inner class Plx : OperationTest(
        "PLX",
        { plx }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Plx8 : Test8BitIndex() {
            @Test
            fun pull() {
                prepareProcessor(x = 0x11, s = 0x0000)

                memory.returnFor(0x00, 0x0001, 0x22)

                testOperation(x = 0x22, s = 0x0001)
            }

            @ParameterizedTest
            @CsvSource(
                "0, false, true",
                "0x80, true, false",
                "0xFF, true, false",
                "0x7F, false, false"
            )
            fun status(fetch: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                memory.returnFor(0x00, 0x0001, fetch)

                status.negative = negative
                status.zero = zero

                testOperation(x = fetch, s = 0x0001)
            }
        }

        @Nested
        inner class Plx16 : Test16BitIndex() {
            @Test
            fun pull() {
                prepareProcessor(x = 0x1122)

                memory.returnFor(0x00, 0x0001, 0x44)
                memory.returnFor(0x00, 0x0002, 0x33)

                testOperation(x = 0x3344, s = 0x0002)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true",
                "0x00, 0x80, 0x8000, true, false",
                "0xFF, 0xFF, 0xFFFF, true, false",
                "0xFF, 0x7F, 0x7FFF, false, false"
            )
            fun status(fetch1: Int, fetch2: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                memory.returnFor(0x00, 0x0001, fetch1)
                memory.returnFor(0x00, 0x0002, fetch2)

                status.negative = negative
                status.zero = zero

                testOperation(x = result, s = 0x0002)
            }
        }
    }

    @Nested
    inner class Ply : OperationTest(
        "PLY",
        { ply }
    ) {
        @BeforeEach
        fun initialize() {
            prepareStatus(s16 = true)
        }

        @Nested
        inner class Ply8 : Test8BitIndex() {
            @Test
            fun pull() {
                prepareProcessor(y = 0x11, s = 0x0000)

                memory.returnFor(0x00, 0x0001, 0x22)

                testOperation(y = 0x22, s = 0x0001)
            }

            @ParameterizedTest
            @CsvSource(
                "0, false, true",
                "0x80, true, false",
                "0xFF, true, false",
                "0x7F, false, false"
            )
            fun status(fetch: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                memory.returnFor(0x00, 0x0001, fetch)

                status.negative = negative
                status.zero = zero

                testOperation(y = fetch, s = 0x0001)
            }
        }

        @Nested
        inner class Ply16 : Test16BitIndex() {
            @Test
            fun pull() {
                prepareProcessor(y = 0x1122)

                memory.returnFor(0x00, 0x0001, 0x44)
                memory.returnFor(0x00, 0x0002, 0x33)

                testOperation(y = 0x3344, s = 0x0002)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, 0, false, true",
                "0x00, 0x80, 0x8000, true, false",
                "0xFF, 0xFF, 0xFFFF, true, false",
                "0xFF, 0x7F, 0x7FFF, false, false"
            )
            fun status(fetch1: Int, fetch2: Int, result: Int, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                memory.returnFor(0x00, 0x0001, fetch1)
                memory.returnFor(0x00, 0x0002, fetch2)

                status.negative = negative
                status.zero = zero

                testOperation(y = result, s = 0x0002)
            }
        }
    }
}