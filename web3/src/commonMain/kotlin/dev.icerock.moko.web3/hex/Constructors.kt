/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("ClassName")

package dev.icerock.moko.web3.hex

/**
 * @param strict if true then there are additional check for odd
 */
fun HexString(value: String, strict: Boolean = true): HexString = HexStringValueClass(value).apply {
    if (strict) {
        require(withoutPrefix.length % 2 == 0) { "Hex string should have an odd length" }
    }
}

/**
 * @param size is byte-equivalent size for string
 */
fun HexString(value: String, size: Int): HexString = HexString(value, strict = true)
    .apply {
        val hexSize = withoutPrefix.length / 2
        require(hexSize == size) { "Hex string should have an $size bytes size, but was $hexSize" }
    }

// Consider inheriting this interfaces only using the factory function for safety

interface Hex8String : HexString {
    companion object : HexString.Factory<Hex8String> {
        override fun createInstance(value: String): Hex8String = Hex8String(value)
    }
}
private class _Hex8String(value: String) : Hex8String, HexString by HexString(value, size = 8)
fun Hex8String(value: String): Hex8String = _Hex8String(value)

interface Hex16String : HexString {
    companion object : HexString.Factory<Hex16String> {
        override fun createInstance(value: String): Hex16String = Hex16String(value)
    }
}
private class _Hex16String(value: String) : Hex16String, HexString by HexString(value, size = 16)
fun Hex16String(value: String): Hex16String = _Hex16String(value)

interface Hex32String : HexString {
    companion object : HexString.Factory<Hex32String> {
        override fun createInstance(value: String): Hex32String = Hex32String(value)
    }
}
private class _Hex32String(value: String) : Hex32String, HexString by HexString(value, size = 32)
fun Hex32String(value: String): Hex32String = _Hex32String(value)

interface Hex64String : HexString {
    companion object : HexString.Factory<Hex64String> {
        override fun createInstance(value: String): Hex64String = Hex64String(value)
    }
}
private class _Hex64String(value: String) : Hex64String, HexString by HexString(value, size = 64)
fun Hex64String(value: String): Hex64String = _Hex64String(value)

interface Hex128String : HexString {
    companion object : HexString.Factory<Hex128String> {
        override fun createInstance(value: String): Hex128String = Hex128String(value)
    }
}
private class _Hex128String(value: String) : Hex128String, HexString by HexString(value, size = 128)
fun Hex128String(value: String): Hex128String = _Hex128String(value)

interface Hex256String : HexString {
    companion object : HexString.Factory<Hex256String> {
        override fun createInstance(value: String): Hex256String = Hex256String(value)
    }
}
private class _Hex256String(value: String) : Hex256String, HexString by HexString(value, size = 256)
fun Hex256String(value: String): Hex256String = _Hex256String(value)
