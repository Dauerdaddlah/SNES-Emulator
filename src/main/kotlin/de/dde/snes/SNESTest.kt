package de.dde.snes

import de.dde.snes.cartridge.Cartridge
import de.dde.snes.controller.Joypad
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

    val snes = SNES()
    snes.controllers.plugIn(Joypad(), 1)
    snes.reset()

    snes.insertCartridge(c)

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