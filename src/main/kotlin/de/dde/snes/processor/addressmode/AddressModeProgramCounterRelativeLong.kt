package de.dde.snes.processor.addressmode

import de.dde.snes.processor.ProcessorAccess
import de.dde.snes.processor.register.Register

class AddressModeProgramCounterRelativeLong(
    processor: ProcessorAccess,
    private val rPC: Register
) : AddressModeSimpleShort(
    "rl",
    "Program Counter Relative Long",
    AddressModeResult.SHORTADDRESS,
    processor
) {
    override fun fetchValue(): Int {
        return super.fetchValue().toShort().toInt() + rPC.get()
    }
}