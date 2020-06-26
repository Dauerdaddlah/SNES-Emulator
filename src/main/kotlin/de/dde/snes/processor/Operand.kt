package de.dde.snes.processor

abstract class Operand(
    val symbol: String,
    val name: String
) {
    abstract fun getValue(): Int

    override fun toString(): String {
        return symbol
    }

    operator fun invoke() = getValue()
}