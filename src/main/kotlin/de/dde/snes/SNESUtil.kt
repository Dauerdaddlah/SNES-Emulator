package de.dde.snes

fun Int.isBitSet(bit: Int) = this and bit != 0
fun Int.setBit(bit: Int) = this or bit

fun Int.lowByte() = this and 0xFF
fun Int.highByte() = this and 0xFF00 shr 8

fun Int.asByte() = this and 0xFF
fun Short(lowByte: Int, highByte: Int) = lowByte or (highByte shl 8)
fun Int.withHigh(highByte: Int) = Short(this.asByte(), highByte)
fun Int.withLow(lowByte: Int) = (this and 0xFF00) or lowByte