/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

data class Web3RpcRequest<TParam, TResult>(
    val method: String,
    val params: List<TParam>,
    val paramsSerializer: SerializationStrategy<TParam>,
    val resultSerializer: DeserializationStrategy<TResult>
)
