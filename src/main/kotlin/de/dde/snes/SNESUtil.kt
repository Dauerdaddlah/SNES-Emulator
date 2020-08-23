package de.dde.snes

import de.dde.snes.memory.Bank
import de.dde.snes.memory.ShortAddress

fun Int.isBitSet(bit: Int) = this and bit != 0
fun Int.setBit(bit: Int) = this or bit

fun Int.lowByte() = this and 0xFF
fun Int.highByte() = this and 0xFF00 shr 8
fun Int.longByte() = this and 0xFF0000 shr 16

fun Int.asByte() = this and 0xFF
fun Int.asShort() = this and 0xFFFF

fun Short(lowByte: Int, highByte: Int) = lowByte or (highByte shl 8)

fun Int.withHigh(highByte: Int) = Short(this.asByte(), highByte)
fun Int.withLow(lowByte: Int) = (this and 0xFF00) or lowByte

fun Int.withLongByte(longByte: Int) = this.asShort() or (longByte shl 16)

fun Int.toHexString() = this.toString(16)
fun Int.toHexString(size: Int) = "%0${size}x".format(this)
fun Bank.toBankString() = this.toHexString(2)
fun ShortAddress.toAddressString() = this.toHexString(4)

fun slog(text: String) {
    println(text)
}