package de.dde.snes.processor

abstract class Instruction(
    val symbol: String,
    val description: String
) {
    abstract fun execute()

    override fun toString(): String {
        return symbol
    }

    operator fun invoke() = execute()
}

abstract class Instruction1(
    symbol: String,
    description: String,
    val operand: Operand
) : Instruction(symbol, description) {
    override fun toString(): String {
        return "$symbol ${operand.symbol}"
    }
}