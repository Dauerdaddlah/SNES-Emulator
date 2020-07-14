package de.dde.snes.processor.addressmode

import de.dde.snes.processor.ProcessorAccess
import de.dde.snes.processor.register.Register
import de.dde.snes.withLongByte

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
    protected val processor: ProcessorAccess
) : AddressModeBase(
    symbol,
    description,
    result
) {
    override fun fetchValue(): Int {
        return processor.fetch()
    }
}

open class AddressModeSimpleRegister(
    symbol: String,
    description: String,
    result: AddressModeResult,
    processor: ProcessorAccess,
    private val register: Register
) : AddressModeSimple(
    symbol,
    description,
    result,
    processor
) {
    override fun fetchValue(): Int {
        return super.fetchValue() + register.get()
    }
}

open class AddressModeSimpleRegister2(
    symbol: String,
    description: String,
    result: AddressModeResult,
    processor: ProcessorAccess,
    register: Register,
    private val register2: Register
) : AddressModeSimpleRegister(
    symbol,
    description,
    result,
    processor,
    register
) {
    override fun fetchValue(): Int {
        return super.fetchValue() + register2.get()
    }
}

open class AddressModeSimpleShort(
    symbol: String,
    description: String,
    result: AddressModeResult,
    private val processor: ProcessorAccess
) : AddressModeBase(
    symbol,
    description,
    result
) {
    override fun fetchValue(): Int {
        return processor.fetchShort()
    }
}

open class AddressModeSimpleShortRegister(
    symbol: String,
    description: String,
    result: AddressModeResult,
    processor: ProcessorAccess,
    private val register: Register
) : AddressModeSimpleShort(
    symbol,
    description,
    result,
    processor
) {
    override fun fetchValue(): Int {
        return super.fetchValue() + register.get()
    }
}

open class AddressModeSimpleLong(
    symbol: String,
    description: String,
    result: AddressModeResult,
    private val processor: ProcessorAccess
) : AddressModeBase(
    symbol,
    description,
    result
) {
    override fun fetchValue(): Int {
        return processor.fetchLong()
    }
}

open class AddressModeSimpleLongRegister(
    symbol: String,
    description: String,
    result: AddressModeResult,
    processor: ProcessorAccess,
    private val register: Register
) : AddressModeSimpleLong(
    symbol,
    description,
    result,
    processor
) {
    override fun fetchValue(): Int {
        return super.fetchValue() + register.get()
    }
}

abstract class AddressModeIndirectBase(
    symbol: String,
    description: String,
    processor: ProcessorAccess,
    private val middleResult: AddressModeResult,
    private val middleResult2: AddressModeResult,
    endResult: AddressModeResult
) : AddressModeSimple(
    symbol, description,
    endResult,
    processor
) {
    override fun fetchValue(): Int {
        var r = super.fetchValue()

        r = prepareAddress1(r)

        val bank1 = processor.getBankFor(middleResult)

        r = when (middleResult2.size) {
            1 -> processor.read(bank1, r)
            2 -> processor.readShort(bank1, r)
            3 -> processor.readLong(bank1, r)
            else -> error("invalid middleResult<$middleResult2> for indirect addressmode")
        }

        val bank2 = processor.getBankFor(middleResult2)

        if (bank2 != -1) {
            r = r.withLongByte(bank2)
        }

        r = prepareAddress2(r)

        return r
    }

    protected abstract fun prepareAddress1(address: Int): Int
    protected abstract fun prepareAddress2(address: Int): Int
}

open class AddressModeIndirectSimple(
    symbol: String,
    description: String,
    processor: ProcessorAccess,
    private val r: Register,
    middleResult: AddressModeResult,
    middleResult2: AddressModeResult,
    endResult: AddressModeResult = middleResult2
) : AddressModeIndirectBase(
    symbol,
    description,
    processor,
    middleResult,
    middleResult2,
    endResult
) {
    override fun prepareAddress1(address: Int): Int {
        return address + r.get()
    }

    override fun prepareAddress2(address: Int): Int {
        return address
    }
}

open class AddressModeIndirectSimple2(
    symbol: String,
    description: String,
    processor: ProcessorAccess,
    r: Register,
    private val r2: Register,
    middleResult: AddressModeResult,
    endResult: AddressModeResult
) : AddressModeIndirectSimple(
    symbol,
    description,
    processor,
    r,
    middleResult,
    endResult
) {
    override fun prepareAddress1(address: Int): Int {
        return super.prepareAddress1(address) + r2.get()
    }
}

open class AddressModeIndirectComplex(
    symbol: String,
    description: String,
    processor: ProcessorAccess,
    r: Register,
    middleResult: AddressModeResult,
    middleResult2: AddressModeResult,
    private val r2: Register,
    endResult: AddressModeResult
) : AddressModeIndirectSimple(
    symbol,
    description,
    processor,
    r,
    middleResult,
    middleResult2,
    endResult
) {
    override fun prepareAddress2(address: Int): Int {
        return address + r2.get()
    }
}