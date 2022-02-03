/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.hex.internal.hexBytesFillToSizedHex
import dev.icerock.moko.web3.hex.internal.hexStringFillToSizedHex

fun HexString.fillToStrict() = fillToSizedHex(
    object : HexString.SizedFactory<HexString>, HexString.Factory<HexString> {
        override val size = this@fillToStrict.size

        override fun createInstance(value: String): HexString = HexString.Lenient.createInstance(value)
        override fun createInstance(value: BigInt): HexString = HexString.Lenient.createInstance(value)
        override fun createInstance(value: ByteArray): HexString = HexString.Lenient.createInstance(value)
    }
)

/**
 * This function adds leading zeros, so the original hex
 * will be padded to required size.
 *
 * HexString("0x10").fillToHex8() - Hex8String("0x0000000000000010")
 */
fun <T : HexString> HexString.fillToSizedHex(typeclass: HexString.SizedFactory<T>): T = when (sourceType) {
    HexString.SourceType.ByteArray -> byteArray
        .hexBytesFillToSizedHex(typeclass.size)
        .let(typeclass::createInstance)
    else -> withoutPrefix
        .hexStringFillToSizedHex(typeclass.size)
        .let(typeclass::createInstance)
}
