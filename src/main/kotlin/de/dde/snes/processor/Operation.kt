package de.dde.snes.processor

interface Operation {
    val symbol: String
    val description: String
    fun execute()
}

abstract class OperationBase(
    override val symbol: String,
    override val description: String
) : Operation {
    override fun toString(): String {
        return symbol
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
    override fun execute() {
        action()
    }
}