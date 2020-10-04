package de.dde.snes.ppu

import de.dde.snes.*
import de.dde.snes.memory.Bank
import de.dde.snes.memory.Memory
import de.dde.snes.memory.MemoryMapping
import de.dde.snes.memory.ShortAddress

class PPU(
    private val snes: SNES
) : MemoryMapping {
    val oam = OAM()
    val vram = VRAM()
    val cgram = CGRAM()
    val window1 = MaskWindow()
    val window2 = MaskWindow()
    val colorMath = ColorMath()

    /** when set, display is deactivated (blank) and vram can be accessed */
    var forceBlank = false
    var brightness = 0x00 // TODO
    var objSize = ObjectSize.byCode(0)
    var nameSelect = 0 // TODO
    var nameBaseSelect = 0
    var mosaicSize = 1

    /** the last value written to one of the bgnoffs-registers 210d-2114 */
    var bgnOffsPrev1 = 0
    /** the pre-last value written to one of the bgnoffs-registers 210d-2114 */
    var bgnOffsPrev2 = 0

    /** whether the counters have been latched, will be reset on reading of 0x213F */
    var countersLatched = false
    /** the current horizontal offset of the beam */
    var hoffset = 0
    /** the last latched horizontal scanline position */
    var hoffsetLatched = 0
    /** the horizontal scanline in 9-bits long but read via 1 register, therefore this tells whether to return the high or low byte */
    var hoffsetHigh = false
    /** the current vertical offset of the beam */
    var voffset = 0
    /** the last latched vertical scanline position */
    var voffsetLatched = 0
    /** the vertical scanline in 9-bits long but read via 1 register, therefore this tells whether to return the high or low byte */
    var voffsetHigh = false

    var inVBlank = false
        private set
    var inHBlank = false
        private set

    var product = 0

    var externalSync = false
    var pseudoHire = false
    var overscan = false
    var objInterlace = false
    var screenInterlace = false

    var timeOver = false
    var rangeOver = false
    var interlaceField = false

    var bg3Prio = false
    var bgMode = 0

    val backgrounds = arrayOf(
        Background("BG1"),
        Background("BG2"),
        Background("BG3"),
        Background("BG4"),
        Background("OBJ")
    )

    val mode7 = Mode7()

    fun reset() {
        vram.reset()
        oam.reset()
        cgram.reset()
        window1.reset()
        window2.reset()
        colorMath.reset()
        mode7.reset()

        for (bg in backgrounds) {
            bg.reset()
        }

        objSize = ObjectSize.byCode(0)
        forceBlank = false
        brightness = 0x00
        nameSelect = 0
        nameBaseSelect = 0
        mosaicSize = 1
        bgnOffsPrev1 = 0
        bgnOffsPrev2 = 0
        hoffset = 0
        hoffsetLatched = 0
        hoffsetHigh = false
        voffset = 0
        voffsetLatched = 0
        voffsetHigh = false
        inVBlank = false
        inHBlank = false
        product = 0
        externalSync = false
        pseudoHire = false
        overscan = false
        objInterlace = false
        screenInterlace = false
        timeOver = false
        rangeOver = false
        interlaceField = false
        countersLatched = false
        bg3Prio = false
        bgMode = 0
    }

    // TODO
    var delta = 0L
    fun updateCycles(cycles: Long, delta: Long) {
        this.delta += delta

        while(this.delta >= CYCLES_PER_TICK) {
            this.delta -= CYCLES_PER_TICK
            hoffset++

            if (hoffset == FIRST_H_OFFSET) {
                inHBlank = false
            }

            if (hoffset == FIRST_H_OFFSET + snes.version.width) {
                inHBlank = true
                snes.dma.forEach { it.doHdmaForScanline(voffset) }
            }

            if (hoffset == FIRST_H_OFFSET + snes.version.width + snes.version.vWidthEnd) {
                hoffset = 0
                voffset++

                if (voffset == FIRST_V_OFFSET) {
                    inVBlank = false
                }

                if (voffset == FIRST_V_OFFSET + snes.version.heigth) {
                    inVBlank = true
                    if (snes.processor.nmiEnabled)
                    snes.processor.nmiRequested = true
                }

                if (voffset == FIRST_V_OFFSET + snes.version.heigth + snes.version.vHeightEnd) {
                    voffset = 0
                }
            }

            if (snes.processor.xIrqEnabled || snes.processor.yIrqEnabled) {
                val xIrq = if (snes.processor.xIrqEnabled) snes.processor.htime else 0 // if only yIRQ, fire it on x-offset 0
                val yIrq = if (snes.processor.yIrqEnabled) snes.processor.vtime else voffset // if only xIRQ fire it on every line

                if (hoffset == xIrq && voffset == yIrq) {
                    snes.processor.irqRequested = true
                }
            }
        }
    }

    fun latchCounter() {
        countersLatched = true
        hoffsetLatched = hoffset
        voffsetLatched = voffset
    }

    // TODO check if access is possible at any time or only in specific blanks
    override fun readByte(bank: Bank, address: ShortAddress): Int {
        return when (address) {
            0x2100 -> {
                var r = brightness
                if (forceBlank) r = r.setBit(0x80)
                return r
            }
            0x2101 -> {
                (objSize.code shl 5) or (nameSelect shl 3) or nameBaseSelect
            }
            0x2102 -> {
                (if (oam.objPrio) 0x80 else 0) or oam.tableSelect
            }
            0x2103 -> {
                oam.address
            }
            0x2104, 0x2105 -> {
                Memory.OPEN_BUS
            }
            0x2106 -> {
                var r = ((mosaicSize - 1) shl 4)
                var i = 1
                for (bg in backgrounds) {
                    if (bg.mosaic) r = r or i

                    i = i shl 1
                }

                r
            }
            in 0x2107..0x210A -> {
                val bg = backgrounds[address - 0x2107]

                bg.tilemapAddress shl 2 + (if (bg.horizontalMirror) 1 else 0) + (if (bg.verticalMirror) 2 else 0)
            }
            0x210B -> {
                backgrounds[0].baseAddress or (backgrounds[1].baseAddress shl 4)
            }
            0x210C -> {
                backgrounds[2].baseAddress or (backgrounds[3].baseAddress shl 4)
            }
            in 0x210D..0x2114,
            0x2115 -> {
                Memory.OPEN_BUS
            }
            0x2116 -> {
                vram.address.lowByte()
            }
            0x2117 -> {
                vram.address.highByte()
            }
            in 0x2118..0x212D -> {
                Memory.OPEN_BUS
            }
            0x212E -> {
                var r = 0
                var i = 0x1
                for (bg in backgrounds) {
                    if (bg.maskMainWindow) r = r.setBit(i)
                    i = i shl 1
                }
                return r
            }
            0x212F -> {
                var r = 0
                var i = 0x1
                for (bg in backgrounds) {
                    if (bg.maskSubWindow) r = r.setBit(i)
                    i = i shl 1
                }
                return r
            }
            in 0x2130..0x2133 -> {
                Memory.OPEN_BUS
            }
            0x2134 -> {
                product.lowByte()
            }
            0x2135 -> {
                product.highByte()
            }
            0x2136 -> {
                product.longByte()
            }
            0x2137 -> {
                if (snes.controllers.programmableIo2Line) {
                    latchCounter()
                }
                Memory.OPEN_BUS
            }
            0x2138 -> {
                oam.read()
            }
            0x2139 -> {
                vram.read(VRAM.IncrementMode.LOW)
            }
            0x213A -> {
                vram.read(VRAM.IncrementMode.HIGH)
            }
            0x213B -> {
                cgram.read()
            }
            0x213C -> {
                val r = if (hoffsetHigh) hoffsetLatched.highByte() else hoffsetLatched.lowByte()
                hoffsetHigh = !hoffsetHigh
                return r
            }
            0x213D -> {
                val r = if (voffsetHigh) voffsetLatched.highByte() else voffsetLatched.lowByte()
                voffsetHigh = !voffsetHigh
                return -r
            }
            0x213E -> {
                var r = 0
                if (timeOver) r = r or 0x80
                if (rangeOver) r = r or 0x40
                //if (masterSlave)
                    r = r or 0x40
                val version = 1
                r = r or version

                r
            }
            0x213F -> {
                hoffsetHigh = false
                voffsetHigh = false

                var r = 0
                if (interlaceField) r = r or 0x80
                if (countersLatched) {
                    r = r or 0x40
                    if (snes.controllers.programmableIo2Line) {
                        countersLatched = false
                    }
                }
                if (snes.version == SNES.Version.PAL) r = r or 0x02
                val version = 2
                r = r or version

                r
            }
            else -> { println("READ PPU ${address.toString(16)}"); error("not implemented yet") }
        }
    }

    override fun writeByte(bank: Bank, address: ShortAddress, value: Int) {
        when (address) {
            0x2100 -> {
                forceBlank = value.isBitSet(0x80)
                brightness = value.asByte()
            }
            0x2101 -> {
                nameBaseSelect = value and 0x7
                nameSelect = value and 0x18
                objSize = ObjectSize.byCode(value and 0xE0 shr 5)
            }
            0x2102 -> {
                oam.objPrio = value.isBitSet(0x80)
                oam.tableSelect = value and 0x01
            }
            0x2103 -> {
                oam.address = value.asByte()
            }
            0x2104 -> {
                oam.write(value)
            }
            0x2105 -> {
                bgMode = value and 0x07
                slog("BGMODE $bgMode")
                bg3Prio = value.isBitSet(0x08)
                backgrounds[0].bigSize = value.isBitSet(0x10)
                backgrounds[1].bigSize = value.isBitSet(0x20)
                backgrounds[2].bigSize = value.isBitSet(0x40)
                backgrounds[3].bigSize = value.isBitSet(0x80)
            }
            0x2106 -> {
                mosaicSize = (value shr 4).asByte() + 1
                backgrounds[0].mosaic = value.isBitSet(0x1)
                backgrounds[1].mosaic = value.isBitSet(0x2)
                backgrounds[2].mosaic = value.isBitSet(0x4)
                backgrounds[3].mosaic = value.isBitSet(0x8)
            }
            in 0x2107..0x210A -> {
                val bg = backgrounds[address - 0x2107]
                bg.tilemapAddress = (value shr 2).asByte()
                bg.horizontalMirror = value.isBitSet(0x1)
                bg.verticalMirror = value.isBitSet(0x2)
            }
            0x210B -> {
                backgrounds[0].baseAddress = value and 0xF
                backgrounds[1].baseAddress = value shr 4 and 0xF
            }
            0x210C -> {
                backgrounds[2].baseAddress = value and 0xF
                backgrounds[3].baseAddress = value shr 4 and 0xF
            }
            in 0x210D..0x2114 -> {
                if (address == 0x210D) {
                    writeByte(bank, M7HOFS, value)
                } else if (address == 0x210E) {
                    writeByte(bank, M7VOFS, value)
                }

                val bg = backgrounds[(address - 0x210D) / 2]
                if (address.isBitSet(1)) {
                    bg.hScroll = (value and 0xFF shl 8) or (bgnOffsPrev1 and 7.inv()) or (bgnOffsPrev2 and 7)
                } else {
                    bg.vScroll = (value and 0xFF shl 8) or bgnOffsPrev1
                }
                bgnOffsPrev2 = bgnOffsPrev1
                bgnOffsPrev1 = value.asByte()
            }
            0x2115 -> {
                vram.addressIncrementMode = if (value.isBitSet(0x80)) VRAM.IncrementMode.HIGH else VRAM.IncrementMode.LOW
                vram.increment = VRAM.Increment.byCode(value and 0x3)
                vram.mapping = VRAM.Mapping.byCode(value shr 2 and 0x3)
            }
            0x2116 -> {
                vram.address = vram.address.withLow(value.asByte())
            }
            0x2117 -> {
                vram.address = vram.address.withHigh(value.asByte())
            }
            0x2118 -> {
                vram.dataWrite = vram.dataWrite.withLow(value.asByte())
                vram.write(VRAM.IncrementMode.LOW)
            }
            0x2119 -> {
                vram.dataWrite = vram.dataWrite.withHigh(value.asByte())
                vram.write(VRAM.IncrementMode.LOW)
            }
            0x211A -> {
                mode7.bigSize = value.isBitSet(0x80)
                mode7.spaceFill = value.isBitSet(0x40)
                mode7.mirrorY = value.isBitSet(0x2)
                mode7.mirrorX = value.isBitSet(0x1)
            }
            0x211B -> {
                mode7.matrixA = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()

                product = mode7.matrixA * mode7.matrixB.asByte()
            }
            0x211C -> {
                mode7.matrixB = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()

                product = mode7.matrixA * mode7.matrixB.asByte()
            }
            0x211D -> {
                mode7.matrixC = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()
            }
            0x211E -> {
                mode7.matrixD = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()
            }
            0x211F -> {
                mode7.centerX = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()
            }
            0x2120 -> {
                mode7.centerY = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()
            }
            0x2121 -> {
                cgram.address = value.asByte()
            }
            0x2122 -> {
                cgram.write(value.asByte())
            }
            0x2123 -> {
                backgrounds[0].window1Inversion = value.isBitSet(0x01)
                backgrounds[0].window1Enabled = value.isBitSet(0x02)
                backgrounds[0].window2Inversion = value.isBitSet(0x04)
                backgrounds[0].window2Enabled = value.isBitSet(0x08)
                backgrounds[1].window1Inversion = value.isBitSet(0x10)
                backgrounds[1].window1Enabled = value.isBitSet(0x20)
                backgrounds[1].window2Inversion = value.isBitSet(0x40)
                backgrounds[1].window2Enabled = value.isBitSet(0x80)
            }
            0x2124 -> {
                backgrounds[2].window1Inversion = value.isBitSet(0x01)
                backgrounds[2].window1Enabled = value.isBitSet(0x02)
                backgrounds[2].window2Inversion = value.isBitSet(0x04)
                backgrounds[2].window2Enabled = value.isBitSet(0x08)
                backgrounds[3].window1Inversion = value.isBitSet(0x10)
                backgrounds[3].window1Enabled = value.isBitSet(0x20)
                backgrounds[3].window2Inversion = value.isBitSet(0x40)
                backgrounds[3].window2Enabled = value.isBitSet(0x80)
            }
            0x2125 -> {
                backgrounds[4].window1Inversion = value.isBitSet(0x01)
                backgrounds[4].window1Enabled = value.isBitSet(0x02)
                backgrounds[4].window2Inversion = value.isBitSet(0x04)
                backgrounds[4].window2Enabled = value.isBitSet(0x08)
                // TODO COLOR WINDOW
                //backgrounds[5].window1Inversion = value.isBitSet(0x10)
                //backgrounds[5].window1Enabled = value.isBitSet(0x20)
                //backgrounds[5].window2Inversion = value.isBitSet(0x40)
                //backgrounds[5].window2Enabled = value.isBitSet(0x80)
            }
            0x2126 -> {
                window1.leftPosition = value.asByte()
            }
            0x2127 -> {
                window1.rightPosition = value.asByte()
            }
            0x2128 -> {
                window1.leftPosition = value.asByte()
            }
            0x2129 -> {
                window1.rightPosition = value.asByte()
            }
            0x212A -> {
                backgrounds[0].maskLogic = MaskWindow.Logic.byCode(value and 0x03)
                backgrounds[1].maskLogic = MaskWindow.Logic.byCode(value and 0x0C)
                backgrounds[2].maskLogic = MaskWindow.Logic.byCode(value and 0x30)
                backgrounds[3].maskLogic = MaskWindow.Logic.byCode(value and 0xC0)
            }
            0x212B -> {
                backgrounds[4].maskLogic = MaskWindow.Logic.byCode(value and 0x03)
                // TODO COLOR WINDOW
                //backgrounds[1].maskLogic = MaskWindow.Logic.byCode(value and 0x0C)
            }
            0x212C -> {
                var i = 1
                for (bg in backgrounds) {
                    bg.enableMainScreen = value.isBitSet(i)
                    i = i shl 1
                }
            }
            0x212D -> {
                var i = 1
                for (bg in backgrounds) {
                    bg.enableSubScreen = value.isBitSet(i)
                    i = i shl 1
                }
            }
            0x212E -> {
                var i = 0x1
                for (bg in backgrounds) {
                    bg.maskMainWindow = value.isBitSet(i)
                    i = i shl 1
                }
            }
            0x212F -> {
                var i = 0x1
                for (bg in backgrounds) {
                    bg.maskMainWindow = value.isBitSet(i)
                    i = i shl 1
                }
            }
            0x2130 -> {
                colorMath.clipMode = ColorMath.ExecutionMode.byCode(value shr 6 and 0x3)
                colorMath.preventMode = ColorMath.ExecutionMode.byCode(value shr 4 and 0x3)
                colorMath.addSubscreen = value.isBitSet(0x2)
                colorMath.directColorMode = value.isBitSet(0x1)
            }
            0x2131 -> {
                colorMath.addSubtract = ColorMath.AddSubtract.byCode(value and 0x80 shr 7)
                colorMath.halfColorMath = value.isBitSet(0x40)
                backgrounds[0].enableColorMath = value.isBitSet(0x01)
                backgrounds[1].enableColorMath = value.isBitSet(0x02)
                backgrounds[2].enableColorMath = value.isBitSet(0x04)
                backgrounds[3].enableColorMath = value.isBitSet(0x08)
                backgrounds[4].enableColorMath = value.isBitSet(0x10)
                // TODO BACKDROP
                //backgrounds[5].enableColorMath = value.isBitSet(0x20)
            }
            0x2132 -> {
                val intensity = value and 0x1F
                val colorPlanes = value and 0xE0 shl 5
                // TODO ???
            }
            0x2133 -> {
                externalSync = value.isBitSet(0x80)
                mode7.extBg = value.isBitSet(0x40)
                pseudoHire = value.isBitSet(0x08)
                overscan = value.isBitSet(0x04)
                objInterlace = value.isBitSet(0x02)
                screenInterlace = value.isBitSet(0x01)
            }
            in 0x2134..0x213F -> {
            }
            M7HOFS -> {
                mode7.hScroll = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()
            }
            M7VOFS -> {
                mode7.vScroll = Short(value.asByte(), mode7.prev)
                mode7.prev = value.asByte()
            }
            else -> { println("WRITE ${value.toString(16)} to PPU ${address.toString(16)}"); error("not implemented yet")}
        }
    }

    companion object {
        const val M7HOFS = 0x10001
        const val M7VOFS = 0x10002

        const val CYCLES_PER_TICK = 4
        /** the first horizontal position per scanline in which we actively project on the screen */
        const val FIRST_H_OFFSET = 22
        /** the first horizontal position per scanline in which we actively project on the screen */
        const val FIRST_V_OFFSET = 1
    }
}

class Mode7 {
    var hScroll = 0
    var vScroll = 0
    var prev = 0
    var matrixA = 0
    var matrixB = 0
    var matrixC = 0
    var matrixD = 0
    var centerX = 0
    var centerY = 0
    var bigSize = false
    /** false for transparent, true for character 0 */
    var spaceFill = false
    var mirrorX = false
    var mirrorY = false
    var extBg = false

    fun reset() {
        hScroll = 0
        vScroll = 0
        prev = 0
        matrixA = 0
        matrixB = 0
        matrixC = 0
        matrixD = 0
        centerX = 0
        centerY = 0
        bigSize = false
        spaceFill = false
        mirrorX = false
        mirrorY = false
        extBg = false
    }
}

enum class ObjectSize {
    _8x8_16x16,
    _8x8_32x32,
    _8x8_64x64,
    _16x16_32x32,
    _16x16_64x64,
    _32x32_64x64,
    _16x32_32x64,
    _16x32_32x32;

    val code get() = ordinal

    val square get() = when (this) {
        _8x8_16x16,
        _8x8_32x32,
        _8x8_64x64,
        _16x16_32x32,
        _16x16_64x64,
        _32x32_64x64 -> true
        _16x32_32x64,
        _16x32_32x32 -> false
    }

    val smallWidth get() = when (this) {
        _8x8_16x16,
        _8x8_32x32,
        _8x8_64x64 -> 8
        _16x32_32x64,
        _16x16_32x32,
        _16x16_64x64,
        _16x32_32x32 -> 16
        _32x32_64x64 -> 32
    }

    val smallHeight get() = when (this) {
        _8x8_16x16,
        _8x8_32x32,
        _8x8_64x64 -> 8
        _16x16_32x32,
        _16x16_64x64 -> 16
        _32x32_64x64,
        _16x32_32x64,
        _16x32_32x32 -> 32
    }

    val bigWidth get() = when (this) {
        _8x8_16x16 -> 16
        _8x8_32x32,
        _16x32_32x64,
        _16x16_32x32,
        _16x32_32x32 -> 32
        _8x8_64x64,
        _16x16_64x64,
        _32x32_64x64 -> 64
    }

    val bigHeight get() = when (this) {
        _8x8_16x16 -> 16
        _8x8_32x32,
        _16x16_32x32,
        _16x32_32x32 -> 32
        _16x32_32x64,
        _8x8_64x64,
        _16x16_64x64,
        _32x32_64x64 -> 64
    }

    companion object {
        fun byCode(code: Int) = values()[code]
    }
}