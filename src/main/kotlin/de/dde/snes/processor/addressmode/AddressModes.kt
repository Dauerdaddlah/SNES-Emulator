package de.dde.snes.processor.addressmode

import de.dde.snes.processor.ProcessorAccess
import de.dde.snes.processor.register.Register

fun AddressModeAbsolute(
    processor: ProcessorAccess
): AddressMode = AddressModeSimpleShort(
    "a",
    "Absolute",
    AddressModeResult.ADDRESS_DBR,
    processor
)

fun AddressModeAbsoluteIndexedIndirect(
    processor: ProcessorAccess,
    rX: Register
): AddressMode = AddressModeSimpleShortRegister(
    "(a,x)",
    "Absolute Indexed Indirect",
    AddressModeResult.ADDRESS_PBR,
    processor,
    rX
)

fun AddressModeAbsoluteIndexedWithX(
    processor: ProcessorAccess,
    rX: Register
): AddressMode = AddressModeSimpleShortRegister(
    "a,x",
    "Absolute Indexed With X",
    AddressModeResult.ADDRESS_DBR,
    processor,
    rX
)

fun AddressModeAbsoluteIndexedWithY(
    processor: ProcessorAccess,
    rY: Register
): AddressMode = AddressModeSimpleShortRegister(
    "a,y",
    "Absolute Indexed With Y",
    AddressModeResult.ADDRESS_DBR,
    processor,
    rY
)

fun AddressModeAbsoluteIndirect(
    processor: ProcessorAccess
): AddressMode = AddressModeSimpleShort(
    "(a)",
    "Absolute Indirect",
    AddressModeResult.ADDRESS_0,
    processor
)

fun AddressModeAbsoluteLong(
    processor: ProcessorAccess
): AddressMode = AddressModeSimpleLong(
    "al",
    "Absolute Long",
    AddressModeResult.FULLADDRESS,
    processor
)

fun AddressModeAbsoluteLongIndexedWithX(
    processor: ProcessorAccess,
    rX: Register
): AddressMode = AddressModeSimpleLongRegister(
    "al,x",
    "Absolute Long Indexed With X",
    AddressModeResult.FULLADDRESS,
    processor,
    rX
)

fun AddressModeDirect(
    processor: ProcessorAccess,
    rD: Register
): AddressMode = AddressModeSimpleRegister(
    "d",
    "Direct",
    AddressModeResult.ADDRESS_0,
    processor,
    rD
)

fun AddressModeDirectIndexedIndirect(
    processor: ProcessorAccess,
    rX: Register,
    rD: Register
): AddressMode = AddressModeIndirectSimple2(
    "(d,x)",
    "Direct Indexed Indirect",
    processor,
    rD,
    rX,
    AddressModeResult.ADDRESS_0,
    AddressModeResult.ADDRESS_DBR
)

fun AddressModeDirectIndexedWithX(
    processor: ProcessorAccess,
    rX: Register,
    rD: Register
): AddressMode = AddressModeSimpleRegister2(
    "d,x",
    "Direct Indexed With X",
    AddressModeResult.ADDRESS_0,
    processor,
    rX,
    rD
)

fun AddressModeDirectIndexedWithY(
    processor: ProcessorAccess,
    rY: Register,
    rD: Register
): AddressMode = AddressModeSimpleRegister2(
    "d,y",
    "Direct Indexed With Y",
    AddressModeResult.ADDRESS_0,
    processor,
    rY,
    rD
)

fun AddressModeDirectIndirect(
    processor: ProcessorAccess,
    rD: Register
): AddressMode = AddressModeIndirectSimple(
    "(d)",
    "Direct Indirect",
    processor,
    rD,
    AddressModeResult.ADDRESS_0,
    AddressModeResult.ADDRESS_DBR
)

fun AddressModeDirectIndirectIndexed(
    processor: ProcessorAccess,
    rY: Register,
    rD: Register
): AddressMode = AddressModeIndirectComplex(
    "(d),y",
    "Direct Indirect Indexed",
    processor,
    rD,
    AddressModeResult.ADDRESS_0,
    AddressModeResult.ADDRESS_DBR,
    rY,
    AddressModeResult.FULLADDRESS
)

fun AddressModeDirectIndirectLong(
    processor: ProcessorAccess,
    rD: Register
): AddressMode = AddressModeIndirectSimple(
    "[d]",
    "Direct Indirect Long",
    processor,
    rD,
    AddressModeResult.ADDRESS_0,
    AddressModeResult.FULLADDRESS
)

fun AddressModeDirectIndirectLongIndexed(
    processor: ProcessorAccess,
    rY: Register,
    rD: Register
): AddressMode = AddressModeIndirectComplex(
    "[d],y",
    "Direct Indirect Long Indexed",
    processor,
    rD,
    AddressModeResult.ADDRESS_0,
    AddressModeResult.FULLADDRESS,
    rY,
    AddressModeResult.FULLADDRESS
)

fun AddressModeImmediate(
    processor: ProcessorAccess
): AddressMode = AddressModeSimple(
    "#",
    "Immediate",
    AddressModeResult.IMMEDIATE,
    processor
)

fun AddressModeStackRelative(
    processor: ProcessorAccess,
    rS: Register
): AddressMode = AddressModeSimpleRegister(
    "d,s",
    "Stack Relative",
    AddressModeResult.ADDRESS_0,
    processor,
    rS
)

fun AddressModeStackRelativeIndirectIndexed(
    processor: ProcessorAccess,
    rY: Register,
    rS: Register
): AddressMode = AddressModeIndirectComplex(
    "(d,s),y",
    "Stack Relative Indirect Indexed",
    processor,
    rS,
    AddressModeResult.ADDRESS_0,
    AddressModeResult.ADDRESS_DBR,
    rY,
    AddressModeResult.FULLADDRESS
)

fun AddressModeBlockMove(): AddressMode = AddressModeNoValue(
    "xyc",
    "Block Move",
    AddressModeResult.NOTHING,
    "Block Move does not provide any value"
)

fun AddressModeImplied(): AddressMode = AddressModeNoValue(
    "i",
    "Implied",
    AddressModeResult.NOTHING,
    "Implied does not provide any value"
)

fun AddressModeStack(): AddressMode = AddressModeNoValue(
    "s",
    "Stack",
    AddressModeResult.NOTHING,
    "Stack does not fetch any value"
)