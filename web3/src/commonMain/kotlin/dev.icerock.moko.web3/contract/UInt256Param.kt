/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.crypto.hexStringToByteArray
import dev.icerock.moko.web3.crypto.toHex

class UInt256Param : Encoder<BigInt> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun encode(data: BigInt): ByteArray {
        val dataByteArray = data.toString(16).hexStringToByteArray()
        return if(dataByteArray.size < 32) ByteArray(32 - dataByteArray.size) + dataByteArray
        else dataByteArray.copyOf(32)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun decode(byteArray: ByteArray): BigInt {
        val value = byteArray.toHex()
        return BigInt(value, 16)
    }
}
