/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi

object BoolParam : StaticEncoder<Boolean> {
    override fun encode(item: Boolean): ByteArray = when (item) {
        true -> UInt256Param.encode(item = 1.bi)
        false -> UInt256Param.encode(item = 0.bi)
    }

    override fun decode(source: ByteArray): Boolean =
        when (UInt256Param.decode(source)) {
            0.bi -> false
            else -> true
        }
}
