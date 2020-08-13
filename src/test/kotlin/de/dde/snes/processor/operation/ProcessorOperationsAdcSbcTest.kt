package de.dde.snes.processor.operation

import de.dde.snes.highByte
import de.dde.snes.lowByte
import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProcessorOperationsAdcSbcTest {
    @Nested
    inner class Adc : OperationTest(
        "ADC",
        { adc }
    ) {
        @Nested
        inner class Adc8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0x12)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0x34)

                testOperation(dbr = 0x12, a = 0x46)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0x12)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x34)

                testOperation(a = 0x46)
            }

            @Test
            fun status() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.adc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a adc $operand")
                    }
                }
            }

            @Test
            fun statusWithCarry() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        status.carry = true
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.adc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a adc $operand with carry")
                    }
                }
            }

            @Test
            fun statusDecimal() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        status.decimal = true
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.adc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a adc $operand decimal")
                    }
                }
            }

            @Test
            fun statusDecimalWithCarry() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        status.decimal = true
                        status.carry = true
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.adc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a adc $operand decimal with carry")
                    }
                }
            }
        }

        @Nested
        inner class Adc16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0x1111)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0x22)
                memory.returnFor(0x12, 0x2333, 0x22)

                testOperation(dbr = 0x12, a = 0x3333)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0x1111)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)
                addressMode.fetchNext(0x22)

                testOperation(a = 0x3333)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x5555, 0x6666",
                "0x7FFF, 0x0001",
                "0x0000, 0xFFFF"
            )
            fun status(a: Int, operand: Int) {
                status.set(0x00)
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.adc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a adc $operand")
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x5555, 0x6666",
                "0x7FFF, 0x0001",
                "0x0000, 0xFFFF"
            )
            fun statusWithCarry(a: Int, operand: Int) {
                status.set(0x00)
                status.carry = true
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.adc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a adc $operand with carry")
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x5555, 0x6666",
                "0x7FFF, 0x0001",
                "0x0000, 0xFFFF"
            )
            fun statusDecimal(a: Int, operand: Int) {
                status.set(0x00)
                status.decimal = true
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.adc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a adc $operand decimal")
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x5555, 0x6666",
                "0x7FFF, 0x0001",
                "0x0000, 0xFFFF"
            )
            fun statusDecimalWithCarry(a: Int, operand: Int) {
                status.set(0x00)
                status.decimal = true
                status.carry = true
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.adc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a adc $operand decimal with carry")
            }
        }
    }

    @Nested
    inner class Sbc : OperationTest(
        "SBC",
        { sbc }
    ) {
        @Nested
        inner class Sbc8 : Test8Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0x33)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0x22)

                status.carry = true

                testOperation(dbr = 0x12, a = 0x10)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0x33)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)

                status.carry = true

                testOperation(a = 0x10)
            }

            @Test
            fun status() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.sbc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a sbc $operand")
                    }
                }
            }

            @Test
            fun statusWithCarry() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        status.carry = true
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.sbc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a sbc $operand with carry")
                    }
                }
            }

            @Test
            fun statusDecimal() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        status.decimal = true
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.sbc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a sbc $operand decimal")
                    }
                }
            }

            @Test
            fun statusDecimalWithCarry() {
                for (a in 0..0xFF) {
                    for (operand in 0..0xFF) {
                        status.set(0x00)
                        status.decimal = true
                        status.carry = true
                        prepareProcessor(a = a)

                        addressMode.result = AddressModeResult.IMMEDIATE
                        addressMode.fetchNext(operand)

                        val result = Algorithms.sbc8(a, status.get(), operand)

                        status.set(result.second)
                        status._break = true
                        testOperation(a = result.first, prefix = "$a sbc $operand decimal with carry")
                    }
                }
            }
        }

        @Nested
        inner class Sbc16 : Test16Bit() {
            @Test
            fun address() {
                prepareProcessor(dbr = 0x12, a = 0x3333)

                addressMode.result = AddressModeResult.ADDRESS_DBR
                addressMode.fetchNext(0x2332)

                memory.returnFor(0x12, 0x2332, 0x22)
                memory.returnFor(0x12, 0x2333, 0x22)

                status.carry = true

                testOperation(dbr = 0x12, a = 0x1110)
            }

            @Test
            fun immediate() {
                prepareProcessor(a = 0x3333)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(0x22)
                addressMode.fetchNext(0x22)

                status.carry = true

                testOperation(a = 0x1110)
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x0000, 0xFFFF",
                "0xFFFF, 0x0000",
                "0x8000, 0x0001"
            )
            fun status(a: Int, operand: Int) {
                status.set(0x00)
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.sbc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a sbc $operand")
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x0000, 0xFFFF",
                "0xFFFF, 0x0000",
                "0x8000, 0x0001"
            )
            fun statusWithCarry(a: Int, operand: Int) {
                status.set(0x00)
                status.carry = true
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.sbc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a sbc $operand with carry")
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x0000, 0xFFFF",
                "0xFFFF, 0x0000",
                "0x8000, 0x0001"
            )
            fun statusDecimal(a: Int, operand: Int) {
                status.set(0x00)
                status.decimal = true
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.sbc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a sbc $operand decimal")
            }

            @ParameterizedTest
            @CsvSource(
                "0x0000, 0x0000",
                "0x0000, 0x0001",
                "0xFFFF, 0xFFFF",
                "0x0000, 0xFFFF",
                "0xFFFF, 0x0000",
                "0x8000, 0x0001"
            )
            fun statusDecimalWithCarry(a: Int, operand: Int) {
                status.set(0x00)
                status.decimal = true
                status.carry = true
                prepareProcessor(a = a)

                addressMode.result = AddressModeResult.IMMEDIATE
                addressMode.fetchNext(operand.lowByte())
                addressMode.fetchNext(operand.highByte())

                val result = Algorithms.sbc16(a, status.get(), operand)

                status.set(result.second)
                testOperation(a = result.first, prefix = "$a sbc $operand decimal with carry")
            }
        }
    }

    // adapted from algorithms.cpp
    object Algorithms {
        // These assume that host machine uses two's complement

        private const val n80 = 0x80
        private const val v40 = 0x40
        private const val d08 = 0x08
        private const val z02 = 0x02
        private const val c01 = 0x01

        fun adc8( a: Int, pStart: Int, operand: Int ): Pair<Int, Int>
        {
            var p = pStart
            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x0F)+(operand and 0x0F)+carry
                if (result > 9)
                    result += 6

                carry = if(result > 0x0F) 1 else 0
                result = (a and 0xF0)+(operand and 0xF0)+(result and 0x0F)+(carry * 0x10)
            }

            // signs of a and operand match, and sign of result doesn't
            if ((a and 0x80) == (operand and 0x80) && (a and 0x80) != (result and 0x80))
            p = p or v40

            if ((p and d08) != 0 && result > 0x9F)
            result += 0x60

            if (result > 0xFF)
                p = p or c01

            if (result and 0x80 != 0)
            p = p or n80

            if ((result and 0xFF) == 0)
            p = p or z02

            return (result and 0xFF) to p
        }

        fun sbc8( a: Int, pStart: Int, operandStart: Int ): Pair<Int, Int>
        {
            var p = pStart
            var operand = operandStart

            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            operand = operand xor 0xFF

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x0F)+(operand and 0x0F)+carry
                if (result < 0x10)
                    result -= 6

                carry = if(result > 0x0F) 1 else 0
                result = (a and 0xF0)+(operand and 0xF0)+(result and 0x0F)+(carry * 0x10)
            }

            // signs of a and operand match, and sign of result doesn't
            if ((a and 0x80) == (operand and 0x80) && (a and 0x80) != (result and 0x80))
            p = p or v40

            if ((p and d08) != 0 && result < 0x100)
            result -= 0x60

            if (result > 0xFF)
                p = p or c01

            if ((result and 0x80) != 0)
            p = p or n80

            if ((result and 0xFF) == 0)
            p = p or z02

            return (result and 0xFF) to p
        }

        fun adc16( a: Int, pStart: Int, operand: Int ): Pair<Int, Int>
        {
            var p = pStart

            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x000F)+(operand and 0x000F)+carry
                if (result > 0x0009)
                    result += 0x0006

                carry = if(result > 0x000F) 1 else 0

                result = (a and 0x00F0)+(operand and 0x00F0)+(result and 0x000F)+carry * 0x10
                if (result > 0x009F)
                    result += 0x0060

                carry = if(result > 0x00FF) 1 else 0

                result = (a and 0x0F00)+(operand and 0x0F00)+(result and 0x00FF)+carry * 0x100
                if (result > 0x09FF)
                    result += 0x0600

                carry = if(result > 0x0FFF) 1 else 0

                result = (a and 0xF000)+(operand and 0xF000)+(result and 0x0FFF)+carry * 0x1000
            }

            // signs of a and operand match, and sign of result doesn't
            if ((a and 0x8000) == (operand and 0x8000) && (a and 0x8000) != (result and 0x8000))
            p = p or v40

            if ((p and d08) != 0 && result > 0x9FFF)
            result += 0x6000

            if (result > 0xFFFF)
                p = p or c01

            if (result and 0x8000 != 0)
            p = p or n80

            if ((result and 0xFFFF) == 0)
            p = p or z02

            return (result and 0xFFFF) to p
        }

        fun sbc16( a: Int, pStart: Int, operandStart: Int ): Pair<Int, Int>
        {
            var p = pStart

            var carry = if((p and c01) != 0) 1 else 0

            p = p and (n80 or v40 or z02 or c01).inv()

            var result: Int

            val operand = operandStart xor 0xFFFF

            if ((p and d08) == 0)
            {
                result = a + operand + carry
            }
            else
            {
                result = (a and 0x000F)+(operand and 0x000F)+carry
                if (result < 0x0010)
                    result -= 0x0006

                carry = if(result > 0x000F) 1 else 0

                result = (a and 0x00F0)+(operand and 0x00F0)+(result and 0x000F)+carry * 0x10
                if (result < 0x0100)
                    result -= 0x0060

                carry = if(result > 0x00FF) 1 else 0

                result = (a and 0x0F00)+(operand and 0x0F00)+(result and 0x00FF)+carry * 0x100
                if (result < 0x1000)
                    result -= 0x0600

                carry = if(result > 0x0FFF) 1 else 0

                result = (a and 0xF000)+(operand and 0xF000)+(result and 0x0FFF)+carry * 0x1000
            }

            // signs of addends match, and sign of result doesn't
            if (((a xor operand) and 0x8000) == 0 && ((a xor result) and 0x8000) != 0)
            p = p or v40

            if ((p and d08) != 0 && result < 0x10000)
            result -= 0x6000

            if (result > 0xFFFF)
                p = p or c01

            if (result and 0x8000 != 0)
            p = p or n80

            if ((result and 0xFFFF) == 0)
            p = p or z02

            return (result and 0xFFFF) to p
        }
    }
}