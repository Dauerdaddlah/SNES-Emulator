package de.dde.snes.memory

import de.dde.snes.*
import de.dde.snes.cartridge.Cartridge
import kotlin.test.assertTrue

class TestMemory : Memory {
    private val reads = mutableMapOf<FullAddress, Read>()
    private val readsNext = mutableMapOf<FullAddress, MutableList<Read>>()

    private val writes = mutableListOf<Write>()

    override fun reset() {
    }

    override fun initializeFor(cartridge: Cartridge) {
    }

    override fun readByte(bank: Bank, address: ShortAddress): Int {
        val a = fullAddress(bank, address)
        return if (readsNext[a]?.isEmpty() != false) {
            reads[a]?.value ?: throw IllegalAccessException("No read for address ${bank.toBankString()}:${address.toAddressString()} defined")
        } else {
            readsNext[a]!!.removeAt(0).value
        }
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        if (writes.isEmpty()) {
            throw IllegalAccessException("No writes expected")
        }

        val w = writes.removeAt(0)

        if (bank != w.bank || address != w.address || value.asByte() != w.value) {
            throw IllegalAccessException("wrong write${Write(bank, address, value)}, expected $w")
        }
    }

    fun returnFor(bank: Bank, address: ShortAddress, value: Int, once: Boolean = true) {
        val r = Read(bank, address, value)
        val a = fullAddress(bank, address)
        if (once) {
            readsNext.putIfAbsent(a, mutableListOf())
            readsNext[a]!!.add(r)
        } else {
            reads[a] = r
        }
    }

    fun returnShortFor(bank: Bank, address: ShortAddress, value: Int, once: Boolean = true) {
        returnFor(bank, address, value.lowByte(), once)
        returnFor(bank, shortAddress(address + 1), value.highByte(), once)
    }

    fun expectWrite(bank: Bank, address: ShortAddress, value: Int) {
        writes.add(Write(bank, address, value))
    }

    fun checkResult() {
        for (key in ArrayList(readsNext.keys)) {
            if (readsNext[key]?.isEmpty() == true) {
                readsNext.remove(key)
            }
        }

        assertTrue(readsNext.isEmpty(), "missing read-calls<$readsNext>")
        assertTrue(writes.isEmpty(), "missing write-calls<$writes>")
    }

    data class Read(val bank: Bank, val address: ShortAddress, val value: Int)

    data class Write(val bank: Bank, val address: ShortAddress, val value: Int)
}