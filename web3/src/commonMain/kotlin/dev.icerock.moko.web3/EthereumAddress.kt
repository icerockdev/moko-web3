/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.crypto.toChecksummedAddress

interface EthereumAddress {
    val value: String

    val bigInt: BigInt get() = value.removePrefix("0x").bi(16)

    val checksummed: ContractAddress get() = ContractAddress(value = value.toChecksummedAddress())
    val isChecksummed: Boolean get() = this == checksummed

    val isValid get(): Boolean {
        return value.uppercase() == value
                || value.lowercase() == value
                || isChecksummed
    }
}
