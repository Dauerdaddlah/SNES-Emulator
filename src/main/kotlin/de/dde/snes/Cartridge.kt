package de.dde.snes

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class Cartridge(
    val path: Path
) {
    val smcHeaderData: ByteBuffer
    val data: ByteBuffer

    val mode: MemoryMapping
    val header: CartridgeHeader

    init {
        var b = Files.readAllBytes(path)

        val smcHeader = when (b.size.rem(0x400)) {
            0 -> false
            0x200 -> true
            else -> error("malformed header")
        }

        if (smcHeader) {
            smcHeaderData = ByteBuffer.wrap(Arrays.copyOfRange(b, 0, 0x200))
            b = Arrays.copyOfRange(b, 0x200, b.size)
        }
        else {
            smcHeaderData = ByteBuffer.allocate(0)
        }

        data = ByteBuffer.wrap(b)
        data.order(ByteOrder.LITTLE_ENDIAN)

        mode = mappings.find { it.isUsed(this) }?: error("invalid mapping")

        header = mode.readHeader(this)
    }
}