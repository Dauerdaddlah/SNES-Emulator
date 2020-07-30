package de.dde.snes.processor.operation

import de.dde.snes.memory.TestMemory
import de.dde.snes.processor.Operation
import de.dde.snes.processor.Processor
import de.dde.snes.processor.ProcessorMode
import de.dde.snes.processor.addressmode.TestAddressMode
import de.dde.snes.processor.register.StatusRegister
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

abstract class OperationTest(
    private val symbol: String,
    operation: Processor.Operations.() -> Operation
) {
    val memory: TestMemory = TestMemory()
    private val processor: Processor = Processor(memory)
    val addressMode: TestAddressMode = TestAddressMode()
    val status: StatusRegister = StatusRegister()
    var mode: ProcessorMode = ProcessorMode.EMULATION
    private val operation = operation(processor.operations)

    fun prepareStatus(a16: Boolean = false, i16: Boolean = false, s16: Boolean = a16 || i16) {
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
        s: Int = 0) {

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
                      s: Int = 0) {

        assertEquals(symbol, operation.symbol, "wrong symbol")

        operation.execute(addressMode)

        assertAll(
            { assertEquals(pc, processor.rPC.get(), "wrong PC") },
            { assertEquals(pbr, processor.rPBR.get(), "wrong PBR") },
            { assertEquals(dbr, processor.rDBR.get(), "wrong dbr") },
            { assertEquals(mode, processor.mode, "wrong processormode") },
            { assertEquals(a, processor.rA.getFull(), "wrong A") },
            { assertEquals(x, processor.rX.get(), "wrong X") },
            { assertEquals(y, processor.rY.get(), "wrong Y") },
            { assertEquals(d, processor.rD.get(), "wrong D") },
            { assertEquals(s, processor.rS.get(), "wrong S") },
            { assertEquals(status.get(), processor.rP.get(), "wrong P") },
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
}