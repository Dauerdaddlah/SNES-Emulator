package de.dde.snes.controller

import de.dde.snes.SNES
import de.dde.snes.highByte
import de.dde.snes.isBitSet
import de.dde.snes.lowByte
import de.dde.snes.memory.Bank
import de.dde.snes.memory.MemoryMapping
import de.dde.snes.memory.ShortAddress

class Controllers(
    val snes: SNES
) : MemoryMapping {
    var controller1: Controller? = null
    var controller2: Controller? = null

    var controller1Data1 = 0
    var controller1Data2 = 0
    var controller2Data1 = 0
    var controller2Data2 = 0

    var programmableIo14201 = true
    var programmableIo14213 = true
    /** whether the physical line is high, this is, if any is high of io1-4201, io1-4213 or io of controller 1 */
    var programmableIo1Line = true
        private set

    var programmableIo24201 = true
    var programmableIo24213 = true
    /** whether the physical line is high, this is, if any is high of io2-4201, io2-4213 or io of controller 2 */
    var programmableIo2Line = true
        private set

    var strobe = false
    /** whether auto-joypad-read is currently active */
    var autoJoypadReadActive = false
        private set
    /** the index of the bit to read next on auto-joypad-read */
    var autoJoypadReadCounter = 0
        private set

    fun reset() {
        programmableIo14201 = true
        programmableIo14213 = true
        programmableIo1Line = true
        programmableIo24201 = true
        programmableIo24213 = true
        programmableIo2Line = true
        strobe = false
        autoJoypadReadActive = false
        autoJoypadReadCounter = 0
    }

    fun plugIn(controller: Controller, port: Int) {
        removeController(port)

        if (port == 1) {
            controller1 = controller
        } else {
            controller2 = controller
        }
    }

    fun removeController(port: Int) {
        if (port == 1) {
            controller1 = null
        } else {
            controller2 = null
        }
    }

    fun startAutoRead() {
        write(4016, 1)
        write(4016, 0)
        autoJoypadReadActive = true
        autoJoypadReadCounter = 0
    }

    fun autoReadNextInput() {
        if (!autoJoypadReadActive) {
            return
        }

        val d1 = read(4016)
        val d2 = read(4017)

        controller1Data1 = (controller1Data1 shl 1) or (d1 and 1)
        controller2Data1 = (controller2Data1 shl 1) or (d1 and 2 shr 1)
        controller1Data2 = (controller1Data2 shl 1) or (d2 and 1)
        controller2Data2 = (controller2Data2 shl 1) or (d2 and 2 shr 1)

        autoJoypadReadCounter++
        autoJoypadReadActive = autoJoypadReadCounter < 16
    }

    override fun readByte(bank: Bank, address: ShortAddress): Int {
        return read(address)
    }

    private fun read(address: ShortAddress): Int {
        return when (address) {
            0x4016 -> {
                var r = 0
                if (strobe || controller1?.readData1() == true) r = r or 1
                if (controller2?.readData1() == true) r = r or 2

                controller1?.clockPulse()

                return r
            }
            0x4017 -> {
                var r = 0
                if (controller1?.readData2() == true) r = r or 1
                if (controller2?.readData2() == true) r = r or 2

                controller2?.clockPulse()

                return r
            }
            0x4201 -> {
                return (if (programmableIo14201) 0x40 else 0) or (if (programmableIo24201) 0x80 else 0)
            }
            0x4213 -> {
                return (if (programmableIo14213) 0x40 else 0) or (if (programmableIo24213) 0x80 else 0)
            }
            0x4218 -> {
                controller1Data1.lowByte()
            }
            0x4219 -> {
                controller1Data1.highByte()
            }
            0x421A -> {
                controller2Data1.lowByte()
            }
            0x421B -> {
                controller2Data1.highByte()
            }
            0x421C -> {
                controller1Data2.lowByte()
            }
            0x421D -> {
                controller1Data2.highByte()
            }
            0x421E -> {
                controller2Data2.lowByte()
            }
            0x421F -> {
                controller2Data2.highByte()
            }
            else -> { println("READ CONTROLLER ${address.toString(16)}"); error("not implemented yet") }
        }
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        write(address, value)
    }

    private fun write(address: ShortAddress, value: Int) {
        when (address) {
            0x4016 -> {
                val newStrobe = value.isBitSet(1)

                if (strobe != newStrobe) {
                    strobe = newStrobe
                    controller1?.strobe(strobe)
                    controller2?.strobe(strobe)
                }
            }
            0x4201 -> {
                programmableIo14201 = value.isBitSet(0x40)
                programmableIo24201 = value.isBitSet(0x80)

                programmableIo1Line = programmableIo14201 && programmableIo14213 && (controller1?.programmableIo ?: true)
                val new2 = programmableIo24201 && programmableIo24213 && (controller2?.programmableIo ?: true)

                if (new2 != programmableIo2Line && !new2) {
                    snes.ppu.latchCounter()
                }

                programmableIo2Line = new2
            }
            0x4213 -> {
                programmableIo14213 = value.isBitSet(0x40)
                programmableIo24213 = value.isBitSet(0x80)
            }
            0x4017,
            in 0x4218..0x421F -> {

            }
            else -> { println("Write ${value.toString(16)} to Controller ${address.toString(16)}"); error("not implemented yet") }
        }
    }
}