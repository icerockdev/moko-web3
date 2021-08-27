/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.crypto

private val HEX_CHARS = "0123456789abcdef".toCharArray()

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
    require(length % 2 == 0) { "Hex string length should be odd" }

    return removePrefix(prefix = "0x")
        .lowercase()
        .map { it }  // Converting to a list of chars to use destructors in map
        .chunked(size = 2)
        .map { (firstPart, secondPart) -> convertOctetToByte(firstPart, secondPart) }
        .toByteArray()
}

private fun convertOctetToByte(firstPart: Char, secondPart: Char): Byte {
    val firstHex = HEX_CHARS.indexOf(firstPart)
    val secondHex = HEX_CHARS.indexOf(secondPart)

    val message = "Hex string cannot contain non-hex symbols"
    require(firstHex != -1) { message }
    require(secondHex != -1) { message }

    val octet = (firstHex shl 4) or secondHex
    return octet.toByte()
}
