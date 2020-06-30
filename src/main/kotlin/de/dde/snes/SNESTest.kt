package de.dde.snes

import de.dde.snes.memory.Memory
import de.dde.snes.processor.Processor
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

fun main() {
    Logger.getLogger("").let { l ->
        l.level = Level.FINEST
        l.handlers.forEach { it.level = Level.FINEST }
    }

    val fileName = "Legend of Zelda, The - A Link to the Past (U) [!].smc"
    //val fileName = "Sim City (U) [!].smc"
    val u = SNES::class.java.classLoader.getResource(fileName) ?: error("file not found")
    val p = Paths.get(u.toURI())

    val c = Cartridge(p)

    println(c.header)

    val snes = SNES(c)

    val processor = snes.processor

    processor.reset()

    var i = 0
    var cc = 0
    var c2 = 0
    val t = measureTimeMillis {
        while (true) {
            cc++
            if (cc == 1000000) {
                c2++
                cc = 0
                println("${c2}")
            }
            //print("$it ")
            processor.executeNextInstruction()

            if (processor.waitForInterrupt) {
                processor.NMI()
                i++
                break
            }
        }
    }

    println("$t - $i")
    //println(m.byte(memory, 0, v and 0xF000 ushr 12, v and 0xFFF).toString(16))
}

/*
fun readHeader(c: Cartridge, m: Mode) {
    val page = if (m == Mode.LOROM) LOROM_HEADER_PAGE else HIROM_HEADER_PAGE
    val buffer = ByteBuffer.allocate(0x30)
    for (i in 0 until 0x30) {
        buffer.put(c[0][page][HEADER_START_INDEX + i])
    }

    buffer.rewind()
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    val maker = buffer.short
    val game = (0..3).map { buffer.get().toChar() }.joinToString("")
    val fixed = (0..6).map { (buffer.get().toLong() and 0xFF) shl (8 * (6 - it)) }.sum()
    val exRamSize = buffer.get()
    val specVersion = buffer.get()
    val cTypeSub = buffer.get()
    val name = (0 until NAME_SIZE).map { buffer.get().toChar() }.joinToString("").trimEnd()
    val mapMode = buffer.get()
    val cType = buffer.get()
    val romSize = buffer.get()
    val ramSize = buffer.get()
    val destination = buffer.get()
    val fixed2 = buffer.get()
    val maskRomVersion = buffer.get()
    val complementCheck = buffer.short
    val checksum = buffer.short

    val h = Header(maker, game, fixed, exRamSize, specVersion, cTypeSub, name, mapMode, cType, romSize, ramSize, destination, fixed2, maskRomVersion, complementCheck, checksum)

    println(h)
}

 */
