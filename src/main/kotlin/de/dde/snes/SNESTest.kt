package de.dde.snes

import de.dde.snes.cartridge.Cartridge
import de.dde.snes.controller.Joypad
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess
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

    snes.reset()

    snes.start()
}