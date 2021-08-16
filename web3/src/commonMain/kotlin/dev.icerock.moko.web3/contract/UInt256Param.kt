/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.crypto.hexStringToByteArray
import dev.icerock.moko.web3.crypto.toHex

object UInt256Param : StaticEncoder<BigInt> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun encode(item: BigInt): ByteArray {
        val dataByteArray = item.toString(16).hexStringToByteArray()
        return if(dataByteArray.size < 32) ByteArray(32 - dataByteArray.size) + dataByteArray
        else dataByteArray.copyOf(32)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun decode(source: ByteArray): BigInt {
        val value = source.toHex()
        return BigInt(value, 16)
    }
}
