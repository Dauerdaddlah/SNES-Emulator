package de.dde.snes.processor.addressmode

import de.dde.snes.processor.ProcessorAccess
import de.dde.snes.processor.register.Register

class AddressModeProgramCounterRelative(
    processor: ProcessorAccess,
    private val rPC: Register
) : AddressModeSimple(
    "r",
    "Program Counter Relative",
    AddressModeResult.SHORTADDRESS,
    processor
) {
    override fun fetchValue(): Int {
        return super.fetchValue().toByte().toInt() + rPC.get()
    }
}