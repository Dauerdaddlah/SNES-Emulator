package de.dde.snes.cartridge

import kotlin.math.pow

data class CartridgeHeader(
    val maker: Short,
    val gameCode: String,
    val fixedValue: Long, // $00
    val exRamSizeRaw: Byte,
    val specVersion: Byte,
    val cTypeSub: Byte,

    val name: String,
    val mapModeRaw: Byte,
    val cTypeRaw: Byte,
    val romSize: Byte,
    val sramSize: Byte,
    val destinationRaw: Byte,
    val fixed2: Byte, // $33
    val maskRomVersion: Byte,
    val complementCheck: Short,
    val checksum: Short,

    val exRamSize: ExRamSize = ExRamSize.byCode(
        exRamSizeRaw
    ),
    val mapMode: MapMode = MapMode.byCode(
        mapModeRaw
    ),
    val romType: RomType = RomType.byCode(
        cTypeRaw
    ),
    val destination: Destination = Destination.byCode(
        destinationRaw
    )
) {
    val romSizeKb = 2.0.pow(romSize.toDouble())
    val sramSizeKb = if (sramSize == 0.toByte()) 0.0 else 2.0.pow(sramSize.toDouble())
}

enum class ExRamSize(val code: Byte) {
    NONE(0),
    _16K(1),
    _64K(2),
    _256K(3),
    _512K(4),
    _1M(5),
    UNKNOWN(-1);

    companion object {
        fun byCode(code: Byte) = values().find { it.code == code } ?: UNKNOWN
    }
}

enum class Destination(val code: Byte) {
    JAPAN(0x00),
    NORTH_AMERICA(0x01),
    EUROPE(0x02),
    SWEDEN_SCANDINAVIA(0x03),
    FINLAND(0x04),
    DENMARK(0x05),
    FRANCE(0x06),
    NETHERLANDS(0x07),
    SPAIN(0x08),
    GERMANY(0x09),
    ITALY(0x0A),
    CHINA(0x0B),
    INDONESIA(0x0C),
    KOREA(0x0D),
    GLOBAL(0x0E),
    CANADA(0x0F),
    BRAZIL(0x10),
    AUSTRALIA(0x11),
    OTHER_1(0x12),
    OTHER_2(0x13),
    OTHER_3(0x14),
    UNKNOWN(-1);

    companion object {
        fun byCode(code: Byte) = values().find { it.code == code } ?: UNKNOWN
    }
}

// meaning of cType
// 00 - ROM only
// 01 - ROM + RAM
// 02 - ROM + RAM + SRAM
//
// Enhancement chips
// 0* - DSP
// 1* - SuperFX
// 2* - OBC1
// 3* - SA-1
// E* - Other
// F* - Custom Chip
//
// *3 - ROM + Enhancement Chip
// *4 - ROM + Enhancement Chip + RAM
// *5 - ROM + Enhancement Chip + RAM + SRAM
// *6 - ROM + Enhancement Chip + SRAM
enum class RomType(val code: Byte) {
    ROM_ONLY(0x00),
    ROM_RAM(0x01),
    ROM_RAM_SRAM(0x02),

    ROM_DSP(0x03),
    ROM_DSP_RAM(0x04),
    ROM_DSP_RAM_SRAM(0x05),
    ROM_DSP_SRAM(0x06),
    DSP_UNKNOWN_7(0x07),

    ROM_SUPERFX(0x13),
    ROM_SUPERFX_RAM(0x14),
    ROM_SUPERFX_RAM_SRAM(0x15),
    ROM_SUPERFX_SRAM(0x16),

    ROM_OBC1(0x23),
    ROM_OBC1_RAM(0x24),
    ROM_OBC1_RAM_SRAM(0x25),
    ROM_OBC1_SRAM(0x26),

    ROM_SA1(0x33),
    ROM_SA1_RAM(0x34),
    ROM_SA1_RAM_SRAM(0x35),
    ROM_SA1_SRAM(0x36),

    ROM_OTHER(0xE3.toByte()),
    ROM_OTHER_RAM(0xE4.toByte()),
    ROM_OTHER_RAM_SRAM(0xE5.toByte()),
    ROM_OTHER_SRAM(0xE6.toByte()),

    ROM_CUSTOM(0xF3.toByte()),
    ROM_CUSTOM_RAM(0xF4.toByte()),
    ROM_CUSTOM_RAM_SRAM(0xF5.toByte()),
    ROM_CUSTOM_SRAM(0xF6.toByte()),

    UNKNOWN(-1);

    val ram = when (code.toInt() and 0x0F) {
        1, 2, 4, 5 -> true
        else -> false
    }

    val sram = when (code.toInt() and 0x0F) {
        2, 5, 6 -> true
        else -> false
    }

    val enhancementChip = code < 0x03

    val dsp = code.toInt() ushr 4 == 0
    val superfx = code.toInt() ushr 4 == 1
    val obc1 = code.toInt() ushr 4 == 2
    val sa1 = code.toInt() ushr 4 == 3
    val otherChip = code.toInt() ushr 4 == 0xE
    val customChip = code.toInt() ushr 4 == 0xF

    companion object {
        fun byCode(code: Byte) = values().find { it.code == code } ?: UNKNOWN
    }
}

enum class MapMode(val code: Byte) {
    LOROM(0x20),
    HIROM(0x21),
    SA1ROM(0x23),
    FAST_LOROM(0x30),
    FAST_HIROM(0x31),
    EX_LOROM(0x32),
    EX_HIROM(0x35),
    UNKNOWN(-1);

    companion object {
        fun byCode(code: Byte) = values().find { it.code == code } ?: UNKNOWN
    }
}