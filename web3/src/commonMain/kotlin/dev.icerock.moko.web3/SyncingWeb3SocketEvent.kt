/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlinx.serialization.Serializable

@Serializable
data class SyncingWeb3SocketEvent(
    val syncing: Boolean,
    val status: Status
) {
    @Serializable
    data class Status(
        val startingBlock: Int,
        val currentBlock: Int,
        val highestBlock: Int,
        val pulledStates: Int,
        val knownStates: Int
    )
}