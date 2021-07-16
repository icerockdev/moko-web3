/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.web3

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi

interface EthereumAddress {
    val value: String

    val bigInt: BigInt get() = value.removePrefix("0x").bi(16)
}
