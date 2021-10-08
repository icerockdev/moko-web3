/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.crypto.createChecksummedAddress
import dev.icerock.moko.web3.hex.HexString

interface ContractAddress : EthereumAddress {
    companion object : HexString.Factory<ContractAddress> {
        override fun createInstance(value: String): ContractAddress = ContractAddress(value)
    }
}

fun ContractAddress(value: String): ContractAddress = _ContractAddress(value)

fun ContractAddress.toChecksummedAddress(): ContractAddress =
    createChecksummedAddress(sourceAddress = this, factoryTypeclass = ContractAddress)

@Suppress("ClassName")
private class _ContractAddress(value: String) : EthereumAddress by EthereumAddress(value), ContractAddress {
    override fun toString() = withoutPrefix 
    override fun hashCode(): Int = withoutPrefix.hashCode()
    override fun equals(other: Any?): Boolean = other is _ContractAddress && withoutPrefix == other.withoutPrefix
}
