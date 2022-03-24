/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("ClassName")

package dev.icerock.moko.web3.hex

import com.soywiz.kbignum.BigInt
import kotlinx.serialization.Serializable

/*
TEMPLATE CODE GENERATION FOR CLASSES BELOW

fun main() {
    val sizes = listOf(8, 16, 20, 32, 64, 128, 256, 512, 1024, 2048)
    val code = buildString {
        for (hexSize in sizes) {
            // language=kotlin
            val code = """
                object Hex${hexSize}StringSerializer : ParametrizedHexStringSerializer<Hex${hexSize}String>(Hex${hexSize}String)
                
                @Serializable(with = Hex${hexSize}StringSerializer::class)
                open class Hex${hexSize}String : HexString {
                    constructor(string: String) : super(string, size = $hexSize)
                    constructor(bigInt: BigInt) : super(bigInt, size = $hexSize)
                    constructor(byteArray: ByteArray) : super(byteArray, size = $hexSize)

                    companion object : SizedFactory<Hex${hexSize}String> {
                        override val size: Int = $hexSize
                        override fun createInstance(value: String): Hex${hexSize}String = Hex${hexSize}String(value)
                        override fun createInstance(value: BigInt): Hex${hexSize}String = Hex${hexSize}String(value)
                        override fun createInstance(value: ByteArray): Hex${hexSize}String = Hex${hexSize}String(value)
                    }
                }

                fun HexString.fillToHex$hexSize() = fillToSizedHex(Hex${hexSize}String)
            """
            appendLine(code.trimMargin())
            appendLine()
            appendLine()
        }
    }
    println(code)
}
*/

// Code generated via script above

object Hex8StringSerializer : ParametrizedHexStringSerializer<Hex8String>(Hex8String)

@Serializable(with = Hex8StringSerializer::class)
open class Hex8String : HexString {
    constructor(string: String) : super(string, size = 8)
    constructor(bigInt: BigInt) : super(bigInt, size = 8)
    constructor(byteArray: ByteArray) : super(byteArray, size = 8)

    companion object : SizedFactory<Hex8String> {
        override val size: Int = 8
        override fun createInstance(value: String): Hex8String = Hex8String(value)
        override fun createInstance(value: BigInt): Hex8String = Hex8String(value)
        override fun createInstance(value: ByteArray): Hex8String = Hex8String(value)
    }
}

fun HexString.fillToHex8() = fillToSizedHex(Hex8String)


object Hex16StringSerializer : ParametrizedHexStringSerializer<Hex16String>(Hex16String)

@Serializable(with = Hex16StringSerializer::class)
open class Hex16String : HexString {
    constructor(string: String) : super(string, size = 16)
    constructor(bigInt: BigInt) : super(bigInt, size = 16)
    constructor(byteArray: ByteArray) : super(byteArray, size = 16)

    companion object : SizedFactory<Hex16String> {
        override val size: Int = 16
        override fun createInstance(value: String): Hex16String = Hex16String(value)
        override fun createInstance(value: BigInt): Hex16String = Hex16String(value)
        override fun createInstance(value: ByteArray): Hex16String = Hex16String(value)
    }
}

fun HexString.fillToHex16() = fillToSizedHex(Hex16String)


object Hex20StringSerializer : ParametrizedHexStringSerializer<Hex20String>(Hex20String)

@Serializable(with = Hex20StringSerializer::class)
open class Hex20String : HexString {
    constructor(string: String) : super(string, size = 20)
    constructor(bigInt: BigInt) : super(bigInt, size = 20)
    constructor(byteArray: ByteArray) : super(byteArray, size = 20)

    companion object : SizedFactory<Hex20String> {
        override val size: Int = 20
        override fun createInstance(value: String): Hex20String = Hex20String(value)
        override fun createInstance(value: BigInt): Hex20String = Hex20String(value)
        override fun createInstance(value: ByteArray): Hex20String = Hex20String(value)
    }
}

fun HexString.fillToHex20() = fillToSizedHex(Hex20String)


object Hex32StringSerializer : ParametrizedHexStringSerializer<Hex32String>(Hex32String)

@Serializable(with = Hex32StringSerializer::class)
open class Hex32String : HexString {
    constructor(string: String) : super(string, size = 32)
    constructor(bigInt: BigInt) : super(bigInt, size = 32)
    constructor(byteArray: ByteArray) : super(byteArray, size = 32)

    companion object : SizedFactory<Hex32String> {
        override val size: Int = 32
        override fun createInstance(value: String): Hex32String = Hex32String(value)
        override fun createInstance(value: BigInt): Hex32String = Hex32String(value)
        override fun createInstance(value: ByteArray): Hex32String = Hex32String(value)
    }
}

fun HexString.fillToHex32() = fillToSizedHex(Hex32String)


object Hex64StringSerializer : ParametrizedHexStringSerializer<Hex64String>(Hex64String)

@Serializable(with = Hex64StringSerializer::class)
open class Hex64String : HexString {
    constructor(string: String) : super(string, size = 64)
    constructor(bigInt: BigInt) : super(bigInt, size = 64)
    constructor(byteArray: ByteArray) : super(byteArray, size = 64)

    companion object : SizedFactory<Hex64String> {
        override val size: Int = 64
        override fun createInstance(value: String): Hex64String = Hex64String(value)
        override fun createInstance(value: BigInt): Hex64String = Hex64String(value)
        override fun createInstance(value: ByteArray): Hex64String = Hex64String(value)
    }
}

fun HexString.fillToHex64() = fillToSizedHex(Hex64String)


object Hex128StringSerializer : ParametrizedHexStringSerializer<Hex128String>(Hex128String)

@Serializable(with = Hex128StringSerializer::class)
open class Hex128String : HexString {
    constructor(string: String) : super(string, size = 128)
    constructor(bigInt: BigInt) : super(bigInt, size = 128)
    constructor(byteArray: ByteArray) : super(byteArray, size = 128)

    companion object : SizedFactory<Hex128String> {
        override val size: Int = 128
        override fun createInstance(value: String): Hex128String = Hex128String(value)
        override fun createInstance(value: BigInt): Hex128String = Hex128String(value)
        override fun createInstance(value: ByteArray): Hex128String = Hex128String(value)
    }
}

fun HexString.fillToHex128() = fillToSizedHex(Hex128String)


object Hex256StringSerializer : ParametrizedHexStringSerializer<Hex256String>(Hex256String)

@Serializable(with = Hex256StringSerializer::class)
open class Hex256String : HexString {
    constructor(string: String) : super(string, size = 256)
    constructor(bigInt: BigInt) : super(bigInt, size = 256)
    constructor(byteArray: ByteArray) : super(byteArray, size = 256)

    companion object : SizedFactory<Hex256String> {
        override val size: Int = 256
        override fun createInstance(value: String): Hex256String = Hex256String(value)
        override fun createInstance(value: BigInt): Hex256String = Hex256String(value)
        override fun createInstance(value: ByteArray): Hex256String = Hex256String(value)
    }
}

fun HexString.fillToHex256() = fillToSizedHex(Hex256String)


object Hex512StringSerializer : ParametrizedHexStringSerializer<Hex512String>(Hex512String)

@Serializable(with = Hex512StringSerializer::class)
open class Hex512String : HexString {
    constructor(string: String) : super(string, size = 512)
    constructor(bigInt: BigInt) : super(bigInt, size = 512)
    constructor(byteArray: ByteArray) : super(byteArray, size = 512)

    companion object : SizedFactory<Hex512String> {
        override val size: Int = 512
        override fun createInstance(value: String): Hex512String = Hex512String(value)
        override fun createInstance(value: BigInt): Hex512String = Hex512String(value)
        override fun createInstance(value: ByteArray): Hex512String = Hex512String(value)
    }
}

fun HexString.fillToHex512() = fillToSizedHex(Hex512String)


object Hex1024StringSerializer : ParametrizedHexStringSerializer<Hex1024String>(Hex1024String)

@Serializable(with = Hex1024StringSerializer::class)
open class Hex1024String : HexString {
    constructor(string: String) : super(string, size = 1024)
    constructor(bigInt: BigInt) : super(bigInt, size = 1024)
    constructor(byteArray: ByteArray) : super(byteArray, size = 1024)

    companion object : SizedFactory<Hex1024String> {
        override val size: Int = 1024
        override fun createInstance(value: String): Hex1024String = Hex1024String(value)
        override fun createInstance(value: BigInt): Hex1024String = Hex1024String(value)
        override fun createInstance(value: ByteArray): Hex1024String = Hex1024String(value)
    }
}

fun HexString.fillToHex1024() = fillToSizedHex(Hex1024String)


object Hex2048StringSerializer : ParametrizedHexStringSerializer<Hex2048String>(Hex2048String)

@Serializable(with = Hex2048StringSerializer::class)
open class Hex2048String : HexString {
    constructor(string: String) : super(string, size = 2048)
    constructor(bigInt: BigInt) : super(bigInt, size = 2048)
    constructor(byteArray: ByteArray) : super(byteArray, size = 2048)

    companion object : SizedFactory<Hex2048String> {
        override val size: Int = 2048
        override fun createInstance(value: String): Hex2048String = Hex2048String(value)
        override fun createInstance(value: BigInt): Hex2048String = Hex2048String(value)
        override fun createInstance(value: ByteArray): Hex2048String = Hex2048String(value)
    }
}

fun HexString.fillToHex2048() = fillToSizedHex(Hex2048String)
