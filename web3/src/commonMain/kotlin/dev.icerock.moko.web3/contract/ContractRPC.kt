/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.moko.web3.contract

import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class ContractRPC(
    val contractAddress: ContractAddress,
    val callData: String,
)
