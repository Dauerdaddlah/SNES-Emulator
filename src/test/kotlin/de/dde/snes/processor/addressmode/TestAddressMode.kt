package de.dde.snes.processor.addressmode

import kotlin.test.assertTrue
import kotlin.test.fail

class TestAddressMode : AddressMode {
    override var symbol: String = ""
    override var description: String = ""
    override var result: AddressModeResult = AddressModeResult.NOTHING

    private val fetches = mutableListOf<Int>()

    override fun fetchValue(): Int {
        if (fetches.isEmpty()) {
            fail("No further fetch defined for AddressMode")
        }

        return fetches.removeAt(0)
    }

    fun fetchNext(value: Int) {
        fetches.add(value)
    }

    fun checkResult() {
        assertTrue(fetches.isEmpty(), "missing fetches on AddressMode")
    }
}