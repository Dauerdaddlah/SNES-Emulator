package de.dde.snes.processor.instruction

import de.dde.snes.processor.Operation
import de.dde.snes.processor.addressmode.AddressMode

data class Instruction(
    val operation: Operation,
    val addressMode: AddressMode
) {
    fun execute() {
        operation.execute(addressMode)
    }

    override fun toString(): String {
        return "$operation $addressMode"
    }
}