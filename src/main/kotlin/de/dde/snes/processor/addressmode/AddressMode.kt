package de.dde.snes.processor.addressmode

interface AddressMode {
    val symbol: String
    val description: String

    val result: AddressModeResult

    fun fetchValue(): Int
}

abstract class AddressModeBase(
    override val symbol: String,
    override val description: String,
    override val result: AddressModeResult
) : AddressMode {
    override fun toString(): String {
        return symbol
    }
}

class AddressModeNoValue(
    symbol: String,
    description: String,
    result: AddressModeResult,
    private val errorMessage: String
) : AddressModeBase(
    symbol,
    description,
    result
) {
    override fun fetchValue(): Int {
        error(errorMessage)
    }
}

open class AddressModeSimple(
    symbol: String,
    description: String,
    result: AddressModeResult,
    val action: () -> Int
) : AddressModeBase(
    symbol,
    description,
    result
) {
    override fun fetchValue(): Int {
        return action()
    }
}