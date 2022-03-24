/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.hex.internal.hexBytesFillToSizedHex
import dev.icerock.moko.web3.hex.internal.hexStringAddLeadingZeroIfNeed
import dev.icerock.moko.web3.hex.internal.hexStringFillToSizedHex
import dev.icerock.moko.web3.hex.internal.hexStringToByteArray
import dev.icerock.moko.web3.hex.internal.toHex
import kotlinx.serialization.Serializable

object StrictHexStringSerializer : ParametrizedHexStringSerializer<HexString>(HexString.Strict)
object LenientHexStringSerializer : ParametrizedHexStringSerializer<HexString>(HexString.Lenient)

// Extending allowed only for making new types
@Serializable(with = StrictHexStringSerializer::class)
open class HexString {
    // This variable used to not store ByteArray, BigInt, String simultaneously independent on source type,
    // but use getters instead
    private val source: Source
    // value that was used for hex creation
    val sourceType: SourceType get() = source.type

    // create from string
    constructor(string: String, strict: Boolean = true) {
        source = object : Source {
            override val withoutPrefix = string.removePrefix(HEX_PREFIX)
            override val bigInt get() = withoutPrefix.bi(RADIX)
            override val byteArray get() = withoutPrefix.hexStringToByteArray(unsafe = true)
            override val size get() = withoutPrefix.length / 2
            override val type get() = SourceType.String
            override fun fastEquals(other: HexString): Boolean? = when (other.sourceType) {
                SourceType.String -> withoutPrefix == other.withoutPrefix
                else -> null
            }
        }
        require(withoutPrefix.matches(Regex("[0-9a-fA-F]*"))) { "Hex string contains not hexadecimal characters" }
        require(!strict || withoutPrefix.length % 2 == 0) { "Hex string should have an odd length" }
    }
    constructor(string: String, size: Int) : this(string, strict = true) {
        require(this.size == size) { "Hex string should have an $size bytes size, but was ${this.size}" }
    }
    constructor(bigInt: BigInt, size: Int? = null) {
        source = object : Source {
            override val withoutPrefix get() = bigInt
                .toString(RADIX)
                .hexStringAddLeadingZeroIfNeed()
                .run {
                    when (size) {
                        null -> this
                        else -> hexStringFillToSizedHex(size)
                    }
                }
            override val bigInt = bigInt
            override val byteArray get() = withoutPrefix
                .hexStringToByteArray()
                .run {
                    when (size) {
                        null -> this
                        else -> hexBytesFillToSizedHex(size)
                    }
                }
            override val size get() = size ?: (withoutPrefix.length / 2)
            override val type = SourceType.BigInt

            override fun fastEquals(other: HexString): Boolean? = when (other.sourceType) {
                SourceType.BigInt -> bigInt == other.bigInt
                else -> null
            }
        }
        // we trigger withoutPrefix here, so this will check if bigInt overflows the size
        if (size != null) try {
            source.withoutPrefix
        } catch (_: IllegalStateException) {
            error("Source BigInt overflows the size. (bigInt=$bigInt,size=$size)")
        }
    }
    constructor(byteArray: ByteArray, size: Int? = null) {
        source = object : Source {
            override val withoutPrefix get() = byteArray.toHex()
            override val bigInt get() = withoutPrefix.bi(RADIX)
            override val byteArray = byteArray
            override val size get() = byteArray.size
            override val type = SourceType.ByteArray

            override fun fastEquals(other: HexString): Boolean? = when (other.sourceType) {
                SourceType.ByteArray -> byteArray.contentEquals(other.byteArray)
                else -> null
            }
        }
        require(size == null || byteArray.size == size) { "ByteArray should have an $size bytes size, but was ${this.size}" }
    }

    val withoutPrefix: String get() = source.withoutPrefix
    val bigInt: BigInt get()  = source.bigInt
    val byteArray: ByteArray get() = source.byteArray
    // hex size in bytes (if not strict, it will return amount of "strict" bytes)
    val size: Int get() = source.size

    val prefixed: String get() = "0x$withoutPrefix"
    // strict hex string should have an odd length, may be false only for HexString created from String
    val strict: Boolean get() = withoutPrefix.length % 2 == 0

    final override fun toString() = prefixed
    final override fun hashCode(): Int = withoutPrefix.hashCode()
    final override fun equals(other: Any?): Boolean = other is HexString &&
            (source.fastEquals(other) ?: (size == other.size && withoutPrefix == other.withoutPrefix))

    private interface Source {
        val withoutPrefix: String
        val bigInt: BigInt
        val byteArray: ByteArray
        val size: Int

        val type: SourceType
        // check if equals may be fast-pathed or return null
        fun fastEquals(other: HexString): Boolean?
    }

    enum class SourceType {
        String, ByteArray, BigInt
    }


    interface Factory<T : HexString> {
        fun createInstance(value: String): T
        fun createInstance(value: ByteArray): T = createInstance(HexString(value).prefixed)
        fun createInstance(value: BigInt): T = createInstance(HexString(value).prefixed)
    }
    interface SizedFactory<T : HexString> : Factory<T> {
        val size: Int

        override fun createInstance(value: ByteArray): T = HexString(value)
            .fillToSizedHex(typeclass = this)

        override fun createInstance(value: BigInt): T = HexString(value)
            .fillToSizedHex(typeclass = this)
    }

    object Strict : Factory<HexString> {
        override fun createInstance(value: String): HexString = HexString(value)
        override fun createInstance(value: ByteArray): HexString = HexString(value)
        override fun createInstance(value: BigInt): HexString = HexString(value)
    }
    object Lenient : Factory<HexString> by Strict {
        override fun createInstance(value: String): HexString = HexString(value, strict = false)
    }

    companion object {
        const val HEX_PREFIX = "0x"
        const val RADIX = 16
    }
}
