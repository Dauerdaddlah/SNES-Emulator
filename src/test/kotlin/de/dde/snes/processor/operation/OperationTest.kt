package de.dde.snes.processor.operation

import de.dde.snes.memory.TestMemory
import de.dde.snes.processor.Operation
import de.dde.snes.processor.Processor
import de.dde.snes.processor.ProcessorMode
import de.dde.snes.processor.addressmode.TestAddressMode
import de.dde.snes.processor.instruction.Instruction
import de.dde.snes.processor.register.StatusRegister
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

abstract class OperationTest(
    private val symbol: String,
    operation: Processor.Operations.() -> Operation
) {
    val memory: TestMemory = TestMemory()
    protected val processor: Processor = Processor(memory)
    val addressMode: TestAddressMode = TestAddressMode()
    val status: StatusRegister = StatusRegister()
    var mode: ProcessorMode = ProcessorMode.EMULATION
    private val operation = operation(processor.operations)

    fun prepareStatus(
        a16: Boolean = mode == ProcessorMode.NATIVE && !status.memory,
        i16: Boolean = mode == ProcessorMode.NATIVE && !status.index,
        s16: Boolean = mode == ProcessorMode.NATIVE || a16 || i16
    ) {
        if ((a16 || i16) && !s16) {
            error("the stackpointer cannot be 8-bit if accumulator or indices are 16-bit")
        }
        if (a16 || i16 || s16) {
            mode = ProcessorMode.NATIVE
            status.emulationMode = false
        }

        if (a16) {
            status.memory = false
        }
        if (i16) {
            status.index = false
        }
    }

    fun prepareProcessor(
        pc: Int = 0,
        pbr: Int = 0,
        dbr: Int = 0,
        a: Int = 0,
        x: Int = 0, y: Int = 0,
        d: Int = 0,
        s: Int = if (mode == ProcessorMode.EMULATION) 0x0100 else 0x0000) {

        processor.mode = mode

        processor.checkRegisterSizes()

        processor.rP.set(status.get())

        processor.checkRegisterSizes()

        processor.rPC.set(pc)
        processor.rPBR.set(pbr)
        processor.rDBR.set(dbr)
        processor.rA.setFull(a)
        processor.rX.set(x)
        processor.rY.set(y)
        processor.rD.set(d)
        processor.rS.set(s)
    }

    fun testOperation(pc: Int = 0,
                      pbr: Int = 0,
                      dbr: Int = 0,
                      a: Int = 0,
                      x: Int = 0, y: Int = 0,
                      d: Int = 0,
                      s: Int = if (mode == ProcessorMode.EMULATION) 0x0100 else 0x0000,
                      prefix: String = "") {

        assertEquals(symbol, operation.symbol, "$prefix wrong symbol")

        processor.execute(Instruction(operation, addressMode))

        assertAll(
            { assertEquals(pc, processor.rPC.get(), "$prefix wrong PC") },
            { assertEquals(pbr, processor.rPBR.get(), "$prefix wrong PBR") },
            { assertEquals(dbr, processor.rDBR.get(), "$prefix wrong dbr") },
            { assertEquals(mode, processor.mode, "$prefix wrong processormode") },
            { assertEquals(a, processor.rA.getFull(), "$prefix wrong A") },
            { assertEquals(x, processor.rX.get(), "$prefix wrong X") },
            { assertEquals(y, processor.rY.get(), "$prefix wrong Y") },
            { assertEquals(d, processor.rD.get(), "$prefix wrong D") },
            { assertEquals(s, processor.rS.get(), "$prefix wrong S") },
            { assertEquals(status.get(), processor.rP.get(), "$prefix wrong P") },
            {
                memory.checkResult()
                addressMode.checkResult()
            }
        )
    }

    abstract inner class Test8Bit {
        @BeforeEach
        fun initialize() {
            prepareStatus(a16 = false)
        }
    }

    abstract inner class Test16Bit {
        @BeforeEach
        fun initialize() {
            prepareStatus(a16 = true)
        }
    }

    abstract inner class Test8BitIndex {
        @BeforeEach
        fun initialize() {
            prepareStatus(i16 = false)
        }
    }

    abstract inner class Test16BitIndex {
        @BeforeEach
        fun initialize() {
            prepareStatus(i16 = true)
        }
    }
}