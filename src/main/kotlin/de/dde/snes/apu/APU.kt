package de.dde.snes.apu

import de.dde.snes.memory.Bank
import de.dde.snes.memory.MemoryMapping
import de.dde.snes.memory.ShortAddress

class APU : MemoryMapping {
    val memory = ByteArray(0x10000)

    var status = 0xAA
    var command = 0xBB
    var address = 0x00

    var transferActive = false
    var initialized = false

    fun reset() {
        transferActive = false
        initialized = false
        status = 0xAA
        address = 0xBB
        address = 0x0000
    }

    // TODO this is just the implementation for a deactivated APU
    override fun readByte(bank: Bank, address: ShortAddress): Int {
        return when (address and 0x3) {
            0x0 -> {
                if (!initialized) {
                    initialized = true
                }
                status
            }
            0x1 -> command
            0x2 -> this.address and 0xFF
            0x3 -> this.address shr 8 and 0xFF
            else -> -1
        }
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        when (address and 0x3) {
            0x0 -> {
                if (!initialized) {
                    return
                }

                val v = value and 0xFF
                val n = (status + 1) and 0xFF

                if (!transferActive && v == 0xCC) {
                    status = 0xCC
                } else if (!transferActive && v == 0 && command != 0) {
                    transferActive = true
                    // transfer command
                    status = v
                } else if (transferActive && v == n) {
                    // transfer command
                    status = v
                } else if (transferActive && v != n) {
                    if (command == 0) {
                        transferActive = false
                        status = v
                        command = 0xBB
                    } else {
                        // start new transfer
                        status = v
                    }
                }
            }
            0x1 -> if (initialized) command = value and 0xFF
            0x2 -> this.address = (this.address and 0xFF00) or (value and 0xFF)
            0x3 -> this.address = (value and 0xFF shl 8) or (this.address and 0xFF)
        }
    }
}