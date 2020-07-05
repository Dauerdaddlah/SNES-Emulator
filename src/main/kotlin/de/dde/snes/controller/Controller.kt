package de.dde.snes.controller

interface Controller {
    fun reset()
    fun strobe(high: Boolean)
    fun clockPulse()
    fun readData1(): Boolean
    fun readData2(): Boolean

    val programmableIo: Boolean
    fun addProgrammableIOListener(listener: (Boolean) -> Any?)
    fun removeProgrammableIoListener(listener: (Boolean) -> Any?)
}

abstract class ControllerNoIo : Controller {
    override val programmableIo: Boolean
        get() = true

    override fun addProgrammableIOListener(listener: (Boolean) -> Any?) {
    }

    override fun removeProgrammableIoListener(listener: (Boolean) -> Any?) {
    }
}