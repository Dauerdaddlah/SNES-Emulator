package de.dde.snes.memory

class HardwareMapping : MemoryMapping {
    override fun readByte(memory: Memory, bank: Bank, address: ShortAddress): Int {
        // TODO
        return when (address) {
            in 0x2100..0x2143,
            in 0x2180..0x2183,
            in 0x4200..0x420D,
            in 0x4210..0x421F,
            in 0x4300..0x430a,
            in 0x4310..0x431a,
            in 0x4320..0x432a,
            in 0x4330..0x433a,
            in 0x4340..0x434a,
            in 0x4350..0x435a,
            in 0x4360..0x436a,
            in 0x4370..0x437a-> { println("read address ${address.toString(16)}"); error("not implemented yet") }
            else -> -1
        }
    }

    override fun writeByte(memory: Memory, bank: Bank, address: ShortAddress, value: Int) {
        // TODO
        when (address) {
            in 0x2100..0x2143,
            in 0x2180..0x2183,
            in 0x4200..0x420D,
            in 0x4210..0x421F,
            in 0x4300..0x430a,
            in 0x4310..0x431a,
            in 0x4320..0x432a,
            in 0x4330..0x433a,
            in 0x4340..0x434a,
            in 0x4350..0x435a,
            in 0x4360..0x436a,
            in 0x4370..0x437a-> println("WRITE ${value.toString(16)} to CPU ${address.toString(16)}")
        }
    }
}