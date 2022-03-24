/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.entity

import kotlinx.serialization.Serializable

@Serializable
data class RpcResponse<out T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: Error? = null
) {
    @Serializable
    data class Error(
        val code: Int,
        val message: String
    )
}
