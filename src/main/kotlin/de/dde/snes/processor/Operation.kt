package de.dde.snes.processor

import de.dde.snes.memory.Bank
import de.dde.snes.memory.ShortAddress
import de.dde.snes.memory.bank
import de.dde.snes.memory.shortAddress
import de.dde.snes.processor.addressmode.AddressMode
import de.dde.snes.processor.addressmode.AddressModeResult
import de.dde.snes.toHexString

interface Operation {
    val symbol: String
    val description: String
    fun execute(addressMode: AddressMode)
}

abstract class OperationBase(
    override val symbol: String,
    override val description: String
) : Operation {
    override fun toString(): String {
        return symbol
    }
}

class OperationSimple(
    symbol: String,
    description: String,
    val action: (AddressMode) -> Any?
) : OperationBase(symbol, description) {
    override fun execute(addressMode: AddressMode) {
        action(addressMode)
    }
}

/**
 * An Operation which executes without any parameter
 */
class OperationSimple0(
    symbol: String,
    description: String,
    val action: () -> Any?
) : OperationBase(symbol, description) {
    override fun execute(addressMode: AddressMode) {
        action()
    }
}

/**
 * An Operation which always executes with one parameter fetched from the addressMode
 */
class OperationSimple1(
    symbol: String,
    description: String,
    val action: (Int) -> Any?
) : OperationBase(symbol, description) {
    override fun execute(addressMode: AddressMode) {
        action(addressMode.fetchValue())
    }
}