package dev.icerock.moko.web3

import kotlinx.serialization.Serializable


@Serializable
data class LogsWeb3SocketEvent(
    val removed: Boolean,
    val logIndex: String,
    val transactionIndex: String,
    val transactionHash: String,
    val blockHash: String,
    val blockNumber: String,
    val address: String,
    val data: String,
    val topics: List<String>
)
