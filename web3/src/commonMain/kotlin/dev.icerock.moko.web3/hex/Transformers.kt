/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex

fun HexString.fillToHex8() = fillToSizedHex(Hex8String)
fun HexString.fillToHex16() = fillToSizedHex(Hex16String)
fun HexString.fillToHex20() = fillToSizedHex(Hex20String)
fun HexString.fillToHex32() = fillToSizedHex(Hex32String)
fun HexString.fillToHex64() = fillToSizedHex(Hex64String)
fun HexString.fillToHex128() = fillToSizedHex(Hex128String)
fun HexString.fillToHex256() = fillToSizedHex(Hex256String)

/**
 * This function adds leading zeros, so the original hex
 * will be padded to required size.
 *
 * HexString("0x10").fillToHex8() - Hex8String("0x0000000000000010")
 */
fun <T> HexString.fillToSizedHex(typeclass: HexString.SizedFactory<T>): T {
    val currentSize = withoutPrefix.length / 2

    if (currentSize > typeclass.size)
        error("This hex string already has more bytes than the target one")
    val isStrictlyValid = withoutPrefix.length % 2 == 0

    return typeclass.createInstance(value = buildString {
        repeat(times = typeclass.size - currentSize) {
            append("00")
        }
        if (!isStrictlyValid)
            append("0")
        append(withoutPrefix)
    })
}
