package de.dde.snes.processor.operation

import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsShiftTest {
    @Nested
    inner class Asl : OperationTest(
        "ASL",
        { asl }
    ) {
        @Nested
        inner class Asl8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x03)

                memory.expectWrite(0x30, 0x1234, 0x06)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_ignoresCarry() {
                status.carry = true

                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x21)

                memory.expectWrite(0x30, 0x1234, 0x42)

                status.carry = false

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_WritesOnly1Byte() {
                prepareProcessor()

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x00, 0x1234, 0x81)

                memory.expectWrite(0x00, 0x1234, 0x02)

                status.carry = true

                testOperation()
            }

            @Test
            fun accumulator() {
                prepareProcessor(a = 0x02)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                testOperation(a = 0x04)
            }

            @Test
            fun accumulator_ignoresCarry() {
                status.carry = true

                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                status.carry = false

                testOperation(a = 0x04)
            }

            @Test
            fun accumulator_OnlySetLowByte() {
                prepareProcessor(a = 0x0102)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                testOperation(a = 0x0104)
            }

            @Test
            fun accumulator_OnlySetLowByte_withCarry() {
                prepareProcessor(a = 0x0181)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x81)

                status.carry = true

                testOperation(a = 0x0102)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, false, true", // zero
                "0xFF, 0xFE, true, true, false",
                "0x40, 0x80, false, true, false", // result negative
                "0x81, 0x02, true, false, false", // result carry
                "0x80, 0x00, true, false, true", // result carry zero
                "0xC0, 0x80, true, true, false", // result carry negative
                "0x55, 0xAA, false, true, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, negative: Boolean, zero: Boolean) {
                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }
        }

        @Nested
        inner class Asl16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x03)
                memory.returnFor(0x30, 0x1235, 0x30)

                memory.expectWrite(0x30, 0x1234, 0x06)
                memory.expectWrite(0x30, 0x1235, 0x60)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_WritesOnly2Bytes() {
                prepareProcessor()
                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x4321)

                memory.returnFor(0x00, 0x4321, 0x01)
                memory.returnFor(0x00, 0x4322, 0x80)

                memory.expectWrite(0x00, 0x4321, 0x02)
                memory.expectWrite(0x00, 0x4322, 0x00)

                status.carry = true

                testOperation()
            }

            @Test
            fun address_ignoresCarry() {
                status.carry = true
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x03)
                memory.returnFor(0x30, 0x1235, 0x30)

                memory.expectWrite(0x30, 0x1234, 0x06)
                memory.expectWrite(0x30, 0x1235, 0x60)

                status.carry = false

                testOperation(dbr = 0x30)
            }

            @Test
            fun accumulator() {
                prepareProcessor(a = 0x1001)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x1001)

                testOperation(a = 0x2002)
            }

            @Test
            fun accumulator_ignoresCarry() {
                status.carry = true

                prepareProcessor(a = 0x1001)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x1001)

                status.carry = false

                testOperation(a = 0x2002)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, false, true", // zero
                "0xFFFF, 0xFFFE, true, true, false",
                "0x4000, 0x8000, false, true, false", // result negative
                "0x8001, 0x0002, true, false, false", // result carry
                "0x8000, 0x0000, true, false, true", // result carry zero
                "0xC000, 0x8000, true, true, false", // result carry negative
                "0x5555, 0xAAAA, false, true, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }
        }
    }

    @Nested
    inner class Lsr : OperationTest(
        "LSR",
        { lsr }
    ) {
        @Nested
        inner class Lsr8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x06)

                memory.expectWrite(0x30, 0x1234, 0x03)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_ignoresCarry() {
                status.carry = true

                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x42)

                memory.expectWrite(0x30, 0x1234, 0x21)

                status.carry = false

                testOperation(dbr = 0x30)
            }

            @Test
            fun accumulator() {
                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x04)

                testOperation(a = 0x02)
            }

            @Test
            fun accumulator_ignoresCarry() {
                status.carry = true

                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x04)

                status.carry = false

                testOperation(a = 0x02)
            }

            @Test
            fun accumulator_OnlySetLowByte() {
                prepareProcessor(a = 0x0104)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x04)

                testOperation(a = 0x0102)
            }

            @Test
            fun accumulator_OnlySetLowByte_withCarry() {
                prepareProcessor(a = 0x0181)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x81)

                status.carry = true

                testOperation(a = 0x0140)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true", // zero
                "0xFF, 0x7F, true, false",
                "0x03, 0x01, true, false", // result carry
                "0x01, 0x00, true, true", // result carry zero
                "0xAA, 0x55, false, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, zero: Boolean) {
                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = false
                status.zero = zero

                testOperation(a = result)
            }
        }

        @Nested
        inner class Lsr16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x06)
                memory.returnFor(0x30, 0x1235, 0x60)

                memory.expectWrite(0x30, 0x1234, 0x03)
                memory.expectWrite(0x30, 0x1235, 0x30)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_ignoresCarry() {
                status.carry = true
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x06)
                memory.returnFor(0x30, 0x1235, 0x60)

                memory.expectWrite(0x30, 0x1234, 0x03)
                memory.expectWrite(0x30, 0x1235, 0x30)

                status.carry = false

                testOperation(dbr = 0x30)
            }

            @Test
            fun accumulator() {
                prepareProcessor(a = 0x2002)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x2002)

                testOperation(a = 0x1001)
            }

            @Test
            fun accumulator_ignoresCarry() {
                status.carry = true

                prepareProcessor(a = 0x2002)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x2002)

                status.carry = false

                testOperation(a = 0x1001)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true", // zero
                "0xFFFF, 0x7FFF, true, false",
                "0x8001, 0x4000, true, false", // result carry
                "0x0001, 0x0000, true, true", // result carry zero
                "0xAAAA, 0x5555, false, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, zero: Boolean) {
                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = false
                status.zero = zero

                testOperation(a = result)
            }
        }
    }

    @Nested
    inner class Rol : OperationTest(
        "ROL",
        { rol }
    ) {
        @Nested
        inner class Rol8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x03)

                memory.expectWrite(0x30, 0x1234, 0x06)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_withCarry() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x81)

                memory.expectWrite(0x30, 0x1234, 0x02)

                status.carry = true

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_WritesOnly1Byte() {
                prepareProcessor()

                addressMode.result = AddressModeResult.ADDRESS_0
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x00, 0x1234, 0x81)

                memory.expectWrite(0x00, 0x1234, 0x02)

                status.carry = true

                testOperation()
            }

            @Test
            fun accumulator() {

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                testOperation(a = 0x04)
            }

            @Test
            fun accumulator_withCarry() {
                status.carry = true

                prepareProcessor(a = 0x0102)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                status.carry = false

                testOperation(a = 0x0105)
            }

            @Test
            fun accumulator_OnlySetLowByte() {
                prepareProcessor(a = 0x0102)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                testOperation(a = 0x0104)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, false, true", // zero
                "0xFF, 0xFE, true, true, false",
                "0x40, 0x80, false, true, false", // result negative
                "0x81, 0x02, true, false, false", // result carry
                "0x80, 0x00, true, false, true", // result carry zero
                "0xC0, 0x80, true, true, false", // result carry negative
                "0x55, 0xAA, false, true, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, negative: Boolean, zero: Boolean) {
                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 1, false, false", // zero
                "0xFF, 0xFF, true, true",
                "0x40, 0x81, false, true", // result negative
                "0x81, 0x03, true, false", // result carry
                "0x80, 0x01, true, false", // result carry zero
                "0xC0, 0x81, true, true", // result carry negative
                "0x55, 0xAB, false, true"
            )
            fun statusWithCarry(fetch: Int, result: Int, carry: Boolean, negative: Boolean) {
                status.carry = true

                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = negative

                testOperation(a = result)
            }
        }

        @Nested
        inner class Rol16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x03)
                memory.returnFor(0x30, 0x1235, 0x30)

                memory.expectWrite(0x30, 0x1234, 0x06)
                memory.expectWrite(0x30, 0x1235, 0x60)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_WritesOnly2Bytes() {
                prepareProcessor()
                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x4321)

                memory.returnFor(0x00, 0x4321, 0x01)
                memory.returnFor(0x00, 0x4322, 0x80)

                memory.expectWrite(0x00, 0x4321, 0x02)
                memory.expectWrite(0x00, 0x4322, 0x00)

                status.carry = true

                testOperation()
            }

            @Test
            fun address_withCarry() {
                status.carry = true
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x03)
                memory.returnFor(0x30, 0x1235, 0x30)

                memory.expectWrite(0x30, 0x1234, 0x07)
                memory.expectWrite(0x30, 0x1235, 0x60)

                status.carry = false

                testOperation(dbr = 0x30)
            }

            @Test
            fun accumulator() {
                prepareProcessor(a = 0x1001)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x1001)

                testOperation(a = 0x2002)
            }

            @Test
            fun accumulator_withCarry() {
                status.carry = true

                prepareProcessor(a = 0x1001)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x1001)

                status.carry = false

                testOperation(a = 0x2003)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, false, true", // zero
                "0xFFFF, 0xFFFE, true, true, false",
                "0x4000, 0x8000, false, true, false", // result negative
                "0x8001, 0x0002, true, false, false", // result carry
                "0x8000, 0x0000, true, false, true", // result carry zero
                "0xC000, 0x8000, true, true, false", // result carry negative
                "0x5555, 0xAAAA, false, true, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, negative: Boolean, zero: Boolean) {
                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = negative
                status.zero = zero

                testOperation(a = result)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0001, false, false", // zero
                "0xFFFF, 0xFFFF, true, true",
                "0x4000, 0x8001, false, true", // result negative
                "0x8001, 0x0003, true, false", // result carry
                "0x8000, 0x0001, true, false", // result carry zero
                "0xC000, 0x8001, true, true", // result carry negative
                "0x5555, 0xAAAB, false, true"
            )
            fun statusWithCarry(fetch: Int, result: Int, carry: Boolean, negative: Boolean) {
                status.carry = true

                prepareProcessor(a = fetch)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = negative

                testOperation(a = result)
            }
        }
    }

    @Nested
    inner class Ror : OperationTest(
        "ROR",
        { ror }
    ) {
        @Nested
        inner class Ror8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x06)

                memory.expectWrite(0x30, 0x1234, 0x03)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_withCarry() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x81)

                memory.expectWrite(0x30, 0x1234, 0x40)

                status.carry = true

                testOperation(dbr = 0x30)
            }

            @Test
            fun accumulator() {
                prepareProcessor(a = 0x04)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x04)

                testOperation(a = 0x02)
            }

            @Test
            fun accumulator_withCarry() {
                status.carry = true

                prepareProcessor(a = 0x0102)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x02)

                status.carry = false
                status.negative = true

                testOperation(a = 0x0181)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true", // zero
                "0xFF, 0x7F, true, false",
                "0x81, 0x40, true, false", // result carry
                "0x01, 0x00, true, true", // result carry zero
                "0xAA, 0x55, false, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, zero: Boolean) {
                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = false
                status.zero = zero

                testOperation(a = result)
            }

            @ParameterizedTest
            @CsvSource(
                "0x00, 0x80, false", // zero
                "0xFF, 0xFF, true",
                "0x81, 0xC0, true", // result carry
                "0x01, 0x80, true", // result carry zero
                "0xAA, 0xD5, false"
            )
            fun statusWithCarry(fetch: Int, result: Int, carry: Boolean) {
                status.carry = true

                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = true
                status.zero = false

                testOperation(a = result)
            }
        }

        @Nested
        inner class Ror16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x06)
                memory.returnFor(0x30, 0x1235, 0x60)

                memory.expectWrite(0x30, 0x1234, 0x03)
                memory.expectWrite(0x30, 0x1235, 0x30)

                testOperation(dbr = 0x30)
            }

            @Test
            fun address_WritesOnly2Bytes() {
                prepareProcessor()
                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x4321)

                memory.returnFor(0x00, 0x4321, 0x01)
                memory.returnFor(0x00, 0x4322, 0x80)

                memory.expectWrite(0x00, 0x4321, 0x00)
                memory.expectWrite(0x00, 0x4322, 0x40)

                status.carry = true

                testOperation()
            }

            @Test
            fun address_withCarry() {
                status.carry = true
                prepareProcessor(dbr = 0x30)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x1234)

                memory.returnFor(0x30, 0x1234, 0x06)
                memory.returnFor(0x30, 0x1235, 0x60)

                memory.expectWrite(0x30, 0x1234, 0x03)
                memory.expectWrite(0x30, 0x1235, 0xB0)

                status.carry = false
                status.negative = true

                testOperation(dbr = 0x30)
            }

            @Test
            fun accumulator() {
                prepareProcessor(a = 0x2002)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x2002)

                testOperation(a = 0x1001)
            }

            @Test
            fun accumulator_withCarry() {
                status.carry = true

                prepareProcessor(a = 0x2002)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(0x2002)

                status.carry = false
                status.negative = true

                testOperation(a = 0x9001)
            }

            @ParameterizedTest
            @CsvSource(
                "0, 0, false, true", // zero
                "0xFFFF, 0x7FFF, true, false",
                "0x8001, 0x4000, true, false", // result carry
                "0x0001, 0x0000, true, true", // result carry zero
                "0xAAAA, 0x5555, false, false"
            )
            fun status(fetch: Int, result: Int, carry: Boolean, zero: Boolean) {
                prepareProcessor()

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = false
                status.zero = zero

                testOperation(a = result)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x8000, false", // zero
                "0xFFFF, 0xFFFF, true",
                "0x8001, 0xC000, true", // result carry
                "0x0001, 0x8000, true", // result carry zero
                "0xAAAA, 0xD555, false"
            )
            fun statusWithCarry(fetch: Int, result: Int, carry: Boolean) {
                status.carry = true

                prepareProcessor(a = fetch)

                addressMode.result = AddressModeResult.ACCUMULATOR
                addressMode.fetchNext(fetch)

                status.carry = carry
                status.negative = true

                testOperation(a = result)
            }
        }
    }
}