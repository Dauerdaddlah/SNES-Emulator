package de.dde.snes.processor.addressmode

import de.dde.snes.processor.register.Register

class AddressModeAccumulator(
    private val rA: Register
) : AddressModeBase(
    "A",
    "Accumulator",
    AddressModeResult.ACCUMULATOR
) {
    override fun fetchValue(): Int {
        return rA.get()
    }
}