package de.dde.snes.ppu

class Background(val name: String) {
    var maskMainWindow = false
    var maskSubWindow = false
    var enableMainScreen = false
    var enableSubScreen = false

    var window1Inversion = false
    var window1Enabled = false
    var window2Inversion = false
    var window2Enabled = false
    var maskLogic = MaskWindow.Logic.byCode(0)

    var mosaic = false

    var tilemapAddress = 0
    var horizontalMirror = false
    var verticalMirror = false

    var baseAddress = 0 // the correct address is 0xthis000
    var hScroll = 0
    var vScroll = 0

    var enableColorMath = false

    var bigSize = true

    fun reset() {
        maskMainWindow = false
        maskSubWindow = false
        enableMainScreen = false
        enableSubScreen = false

        window1Inversion = false
        window1Enabled = false
        window2Inversion = false
        window2Enabled = false
        maskLogic = MaskWindow.Logic.byCode(0)

        mosaic = false

        tilemapAddress = 0
        horizontalMirror = false
        verticalMirror = false

        baseAddress = 0
        hScroll = 0
        vScroll = 0

        enableColorMath = false
        bigSize = false
    }
}