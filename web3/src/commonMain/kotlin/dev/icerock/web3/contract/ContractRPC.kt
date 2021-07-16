/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

package dev.icerock.web3.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.web3.serializer.BigIntSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class ContractRPC(
    val to: String,
    val from: String?,
    val data: String,
    val value: BigInt?
)
