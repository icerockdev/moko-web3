/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.crypto

@OptIn(ExperimentalStdlibApi::class)
private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(): String {
    val result = StringBuilder()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}

fun String.hexStringToByteArray(): ByteArray {
    val processedString = if (length.rem(2) != 0) "0$this"
    else this

    val result = ByteArray(processedString.length / 2)
    val uppercased = processedString.toUpperCase()

    for (i in 0 until processedString.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(uppercased[i]);
        val secondIndex = HEX_CHARS.indexOf(uppercased[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }

    return result
}
