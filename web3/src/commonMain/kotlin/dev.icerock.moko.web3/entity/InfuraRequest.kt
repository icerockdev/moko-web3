/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.entity

import kotlinx.serialization.Serializable

@Serializable
data class InfuraRequest<T>(
    val jsonrpc: String,
    val id: Int,
    val method: String,
    val params: List<T>
) {
    constructor(method: String, params: List<T>) : this(
        jsonrpc = "2.0",
        id = 0,
        method = method,
        params = params
    )
}