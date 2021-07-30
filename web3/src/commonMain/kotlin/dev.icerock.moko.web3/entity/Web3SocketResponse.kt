package dev.icerock.moko.web3.entity

import kotlinx.serialization.Serializable

/**
 * data class to handle incoming messages from web socket connection
 * @param jsonrpc version of json-rpc request
 * @param id id to filter by
 * @param result current identification for request with the same id
 * @param method current method
 * @param params information for subscribed events
 */
@Serializable
data class Web3SocketResponse(
    val jsonrpc: String,
    val id: Int? = null,
    val result: String? = null,
    val method: String? = null,
    val params: Web3SocketResponseParams? = null
)

/**
 * data class that represents parameters info
 */
@Serializable
data class Web3SocketResponseParams(
    val subscription: String,
    val result: String
)