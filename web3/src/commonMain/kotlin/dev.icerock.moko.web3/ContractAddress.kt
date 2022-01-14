/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.crypto.createChecksummedAddress
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.ParametrizedHexStringSerializer
import kotlinx.serialization.Serializable

object ContractAddressSerializer : ParametrizedHexStringSerializer<ContractAddress>(ContractAddress)

@Serializable(with = ContractAddressSerializer::class)
open class ContractAddress(value: String) : EthereumAddress(value) {
    companion object : SizedFactory<ContractAddress> {
        override val size = 20
        override fun createInstance(value: String): ContractAddress = ContractAddress(value)
    }

    override fun toChecksummedAddress(): ContractAddress =
        createChecksummedAddress(sourceAddress = this, factoryTypeclass = ContractAddress)
}
