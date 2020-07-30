package de.dde.snes.processor

import de.dde.snes.SNES
import de.dde.snes.processor.addressmode.AddressModeResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.Paths

class ProcessorInstructionsTest {
    val processor = SNES().processor

    @TestFactory
    fun testInstructionsBySymbol()
        = with (Files.readAllLines(Paths.get(ProcessorInstructionsTest::class.java.getResource("instructionlist.txt").toURI()))) {
        (0 until this.size step 2).map {
            val opCode = it / 2
            val result = this[it]
            DynamicTest.dynamicTest("Test instruction ${"%02x".format(opCode)}") {
                val expectedOperation = result.substring(0, 3)
                val expectedAddressMode = result.substring(3).trim()

                val instruction = processor.getInstruction(opCode)

                Assertions.assertEquals(
                    expectedOperation to expectedAddressMode,
                    instruction.operation.toString() to instruction.addressMode.toString(),
                    "false instruction for opCode ${"%02x".format(opCode)}"
                    )
            }
        }
    }

    @Test
    fun test_branch_addressModes() {
        for (opCode in 0..0xFF) {
            val inst = processor.getInstruction(opCode)

            val symbol = inst.operation.symbol

            when {
                symbol == "bit" || symbol == "brk" -> {
                }
                symbol == "brl" || symbol == "jml" -> {
                    Assertions.assertEquals(AddressModeResult.FULLADDRESS, inst.addressMode.result, "instruction<$inst> has a long branch-operation but a short addressMode")
                }
                symbol[0] == 'b' || symbol == "jmp" -> {
                    Assertions.assertEquals(AddressModeResult.SHORTADDRESS, inst.addressMode.result, "instruction<$inst> has a short branch-operation but a long addressMode")
                }
            }
        }
    }
}