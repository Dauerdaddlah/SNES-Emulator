package de.dde.snes

import de.dde.snes.cartridge.Cartridge
import de.dde.snes.controller.Joypad
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

fun main() {
    //test()
    Logger.getLogger("").let { l ->
        l.level = Level.FINEST
        l.handlers.forEach { it.level = Level.FINEST }
    }

    val fileName = "Legend of Zelda, The - A Link to the Past (Germany).sfc"
    //val fileName = "Sim City (U) [!].smc"
    val u = SNES::class.java.classLoader.getResource(fileName) ?: error("file not found")
    val p = Paths.get(u.toURI())

    val c = Cartridge(p)

    println(c.header)

    val snes = SNES()
    snes.controllers.plugIn(Joypad(), 1)
    snes.reset()

    snes.insertCartridge(c)

    snes.reset()

    snes.start()
}

fun test() {
    val rOld = Files.newBufferedReader(Paths.get("old0.log"))
    val rNew = Files.newBufferedReader(Paths.get("new0.log"))

    Files.createDirectory(Paths.get("old"))
    Files.createDirectory(Paths.get("new"))

    var i = 0
    var wOld = Files.newBufferedWriter(Paths.get("old", "log$i.log"))
    var wNew = Files.newBufferedWriter(Paths.get("new", "log$i.log"))

    val diff = mutableSetOf<Int>()
    var c = 0
    while (true) {
        val sOld = rOld.readLine()
        val sNew = rNew.readLine()

        if(sOld == null || sNew == null) {
            break
        }

        wOld.write(sOld)
        wOld.newLine()
        wNew.write(sNew)
        wNew.newLine()

        c++

        if (sOld != sNew) {
            diff.add(i)
        }

        if (c == 20000) {
            i++
            c = 0
            wOld = Files.newBufferedWriter(Paths.get("old", "log$i.log"))
            wNew = Files.newBufferedWriter(Paths.get("new", "log$i.log"))
        }
    }

    wOld.flush()
    wNew.flush()

    println(diff)

    exitProcess(0)
}