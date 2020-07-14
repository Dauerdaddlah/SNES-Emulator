package de.dde.snes.processor

// alles was auf dem Accumulator läuft (ASL, EOR, AND, etc.) muss je nach A 1 oder 2 Byte holen/geben
// alles was auf dem X/Y läuft (CPX) muss je nach X/Y 1 oder 2 Byte holen/geben

// branch hat immer addressmode r/rl
// jmp hat immer addressmode mit richtiger addresse
// jmp mit long-addressmode = JML -> PBR wird auch gesetzt (5c, dc)
// selbige logic für jsr -> jsl in 22

// transfer setzt flags außer S ist das Ziel

// lesende     aktionen BIT, CMP, CPX, CPY
// Register    aktionen ADC, AND, EOR, ORA, SBC
// schreibende aktionen ASL, ASR, ROL, ROR, DEC, DEX, DEY, INC, INX, INY, DEC, DEX, DEY, TRB, TSB

// overflow wird nur gesetzt von ADC, SBC, BIT
// overflow ist, wenn nach BIT Bit 6 gesetzt ist
// overflow ist bei adc, sbc ein zu großer Wert kommt (in 8 Bit > 127 oder < -128) (Wert hat Vorzeichen 1, Parameter hat Vorzeichen 1 Ergebnis hat Vorzeichen 2)

// shift setzt carry auf den wert des Bit, welches "rausgeshiftet" wird
// adc setzt carry, wenn das ergebnis nicht mehr in das Register passt (bei 8 Bit > 0xFF)
// sbc setzt carry, wenn kein carry benötigt wurde (bei positiven Werten ist das Ergebnis > 0 Bsp. 0x37 - 0x18 -> 0x1F | carry clear bei 0x18 - 0x20 -> 0x1FE)
// selbe Logic von CMP wie bei SBC - carry wird gesetzt wnn das Register kleiner ist als der operand

// NZVC - ADC, SBC
// NZ - AND, DEC, EOR, INC, LOAD, ORA, Transfer ohne S als Ziel
// NZC - ASL, CMP, LSR, ROL, ROR
// NVZ - BIT
// Z - BitImmediate, TRB, TSB

class Processor2 {
    fun instAdc(value: Int): Int {
        // NVZC
        return 0
    }

    fun and8(value: Int, value2: Int): Int {
        // NZ
        return 0
    }

    fun instAsl(value: Int): Int {
        // NZC
        return 0
    }

    fun instBranchIf() {

    }

    fun instBit(value: Int) {
        // NVZ
    }

    fun instBitImmediate(value: Int) {
        // Z
    }

    fun instBrk() {

    }

    fun instSetPValue() {

    }

    fun instCmp(value: Int) {
        // NZC
    }

    fun instCop() {

    }

    fun instDec(value: Int): Int {
        // NZ
        return 0
    }

    fun instEor(value: Int) {
        // NZ
    }

    fun instInc(value: Int): Int {
        // NZ
        return 0
    }

    fun instJump() {

    }

    fun instJumpSubroutine() {

    }

    fun instLoad(): Int {
        // NZ
        return 0
    }

    fun instLSR() {
        // NZC
    }

    fun instBlockMove() {

    }

    fun instNop() {

    }

    fun instOra() {
        // NZ
    }

    fun instPush() {

    }

    fun instPull() {

    }

    fun instRol() {
        // NZC
    }

    fun instRor() {
        // NZC
    }

    fun instRTI() {

    }

    fun instRTS() {

    }

    fun instSBC() {
        // NVZC
    }

    fun instStore() {

    }

    fun instStp() {

    }

    fun instTransfer() {
        // NZ
        // wenn Ziel = S -> kein Flag setzen
    }

    fun isntTrb() {
        // Z
    }

    fun instTsb() {
        // Z
    }

    fun instWai() {

    }

    fun instXba() {

    }

    fun instXce() {

    }
}