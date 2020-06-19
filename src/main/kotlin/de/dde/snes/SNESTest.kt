package de.dde.snes

import snes.SNES
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val fileName = "Legend of Zelda, The - A Link to the Past (U) [!].smc"
    //val fileName = "Sim City (U) [!].smc"
    val u = SNES::class.java.classLoader.getResource(fileName) ?: error("file not found")
    val p = Paths.get(u.toURI())

    val c = Cartridge(p)

    val memory = Memory(c)

    println(c.header)

    val m = c.mode

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
