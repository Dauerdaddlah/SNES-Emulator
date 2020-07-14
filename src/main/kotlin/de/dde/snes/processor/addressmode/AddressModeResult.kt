package de.dde.snes.processor.addressmode

enum class AddressModeResult(
    val size: Int
) {
    SHORTADDRESS(2),
    FULLADDRESS(3),
    ADDRESS_PBR(2),
    ADDRESS_0(2),
    ADDRESS_DBR(2),
    IMMEDIATE(1),
    ACCUMULATOR(2),
    NOTHING(0);

    val value get() = this == IMMEDIATE || this == ACCUMULATOR
    val address get() = this != IMMEDIATE && this != NOTHING && this != ACCUMULATOR
}