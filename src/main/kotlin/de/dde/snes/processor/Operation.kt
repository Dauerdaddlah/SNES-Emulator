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

/**
 * An Operation, which executes on a memory-address
 */
open class OperationSimpleAddress(
    symbol: String,
    description: String,
    val processor: ProcessorAccess,
    val action: (Bank, ShortAddress) -> Any?
) : OperationBase(symbol, description) {
    override fun execute(addressMode: AddressMode) {
        val v = addressMode.fetchValue()

        val bank = when (addressMode.result) {
            AddressModeResult.ADDRESS_0 -> 0
            AddressModeResult.FULLADDRESS -> v.bank
            AddressModeResult.SHORTADDRESS,
            AddressModeResult.ADDRESS_DBR,
            AddressModeResult.ADDRESS_PBR -> processor.getBankFor(addressMode.result)
            else -> error("Invalid addressmode<$addressMode> given for operand<$symbol> ($description)")
        }
        val address = when (addressMode.result) {
            AddressModeResult.ADDRESS_PBR,
            AddressModeResult.ADDRESS_0,
            AddressModeResult.ADDRESS_DBR,
            AddressModeResult.SHORTADDRESS -> v
            AddressModeResult.FULLADDRESS -> v.shortAddress
            else -> error("invalid addressmode<$addressMode> given for operation<$symbol> ($description)")
        }

        action(bank, address)
    }
}