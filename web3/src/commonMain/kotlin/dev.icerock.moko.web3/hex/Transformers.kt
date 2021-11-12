/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex

val Hex8String.asHex8 get() = convertToSizedHex(Hex8String)
val Hex16String.asHex16 get() = convertToSizedHex(Hex16String)
val Hex20String.asHex20 get() = convertToSizedHex(Hex20String)
val HexString.asHex32 get() = convertToSizedHex(Hex32String)
val Hex64String.asHex64 get() = convertToSizedHex(Hex64String)
val Hex128String.asHex128 get() = convertToSizedHex(Hex128String)
val Hex256String.asHex256 get() = convertToSizedHex(Hex256String)

fun <T> HexString.convertToSizedHex(typeclass: HexString.SizedFactory<T>): T {
    val currentSize = withoutPrefix.length / 2

    if (currentSize > typeclass.size)
        error("This hex string has more bytes than the target one")
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
