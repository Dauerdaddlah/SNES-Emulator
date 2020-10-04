package de.dde.snes

import java.io.StringWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

class SnesLog(
    val fileName: String,
    val stopCount: Int = 20000,
    val enabled: Boolean = true
) {
    var writer: Writer

    var i = 0

    init {
        val p = Paths.get("$fileName$i.log")
        writer = if (enabled) {
            Files.newBufferedWriter(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
        } else {
            StringWriter()
        }
    }

    var a: Int = 0
    var x: Int = 0
    var y: Int = 0
    var s: Int = 0
    var d: Int = 0
    var p: Int = 0
    var pc: Int = 0
    var dbr: Int = 0
    var pbr: Int = 0
    var emulation = false

    fun prepare(a: Int, x: Int, y: Int, s: Int, d: Int, p: Int, pc: Int, dbr: Int, pbr: Int, emulation: Boolean) {
        this.a = a
        this.x = x
        this.y = y
        this.s = s
        this.d = d
        this.p = p and 0xFF
        this.pc = pc
        this.dbr = dbr
        this.pbr = pbr
        this.emulation = emulation
    }

    var counter = 0

    fun log(instName: String, address: Int, value: Int) {
        if (!enabled) {
            return
        }
        val sj = StringJoiner("\t", "", System.lineSeparator())

        sj.add("${pbr.toHexString(2)}:${pc.toHexString(4)}")
        sj.add(instName)
        sj.add((address and 0xFFFF).toHexString(4))
        sj.add((value and 0xFFFF).toHexString(4))
        sj.add(if (emulation) "E" else "N")
        sj.add(p.toHexString(2))
        sj.add(a.toHexString(4))
        sj.add(x.toHexString(4))
        sj.add(y.toHexString(4))
        sj.add(s.toHexString(4))
        sj.add(d.toHexString(4))
        sj.add(dbr.toHexString(4))

        writer.write(sj.toString())

        counter++

        if (counter == stopCount) {
            writer.flush()
            writer.close()
            slog("WROTE FILE $i")
            counter = 0
            i++

            if (i == 1) {
                error("fail")
            }

            val p = Paths.get("$fileName$i.log")
            writer = if (enabled) {
                Files.newBufferedWriter(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
            } else {
                StringWriter()
            }
        }
    }
}