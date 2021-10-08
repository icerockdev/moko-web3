/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.hex

import com.soywiz.kbignum.BigInt

interface HexString {
    val withoutPrefix: String
    val prefixed: String
    val bigInt: BigInt

    interface Factory<T> {
        fun createInstance(value: String): T
    }

    companion object : Factory<HexString> {
        override fun createInstance(value: String): HexString = HexString(value)
    }
}
