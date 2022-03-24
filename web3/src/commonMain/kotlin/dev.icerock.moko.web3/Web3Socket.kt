/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.entity.RpcRequest
import dev.icerock.moko.web3.entity.Web3SocketResponse
import dev.icerock.moko.web3.websockets.SubscriptionParam
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Class to work with webSocket connection in Etherium network
 * @param httpClient client to work with WebSocket
 */
class Web3Socket(
    private val httpClient: HttpClient,
    private val webSocketUrl: String,
    private val coroutineScope: CoroutineScope
) {
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
    /**
     * channel to receive data from webSocket
     */
    private val responsesFlowSource: MutableSharedFlow<Web3SocketResponse<JsonElement>> =
        MutableSharedFlow()

    val responsesFlow: SharedFlow<Web3SocketResponse<JsonElement>> = responsesFlowSource.asSharedFlow()

    /**
     * incremental field to filter different incoming messages from websocket
     */
    private var queueID: Int = 0

    /**
     * subscription filter's flow, here we emit new
     */
    private val requestsChannel: Channel<RpcRequest<JsonElement>> = Channel(capacity = 1)

    init {
        // launch websocket connection to work with in over web3Socket lifecycle
        coroutineScope.launch {
            httpClient.webSocket(webSocketUrl) {
                requestsChannel
                    .consumeAsFlow()
                    .map { request ->
                        json.encodeToString(
                            serializer = RpcRequest.serializer(JsonElement.serializer()),
                            value = request
                        )
                    }
                    .map(Frame::Text)
                    .onEach(outgoing::send)
                    .launchIn(scope = this)

                incoming
                    .consumeAsFlow()
                    .mapNotNull { frame ->
                        val textFrame = frame as? Frame.Text
                        return@mapNotNull textFrame?.readText()
                    }
                    .map { text ->
                        json.decodeFromString(
                            deserializer = Web3SocketResponse.serializer(JsonElement.serializer()),
                            string = text
                        )
                    }
                    .collect {
                        responsesFlowSource.emit(it)
                    }
            }
        }
    }

    suspend inline fun <reified T> sendRpcRequest(request: RpcRequest<JsonElement>): T? {
        val response = sendRpcRequestRaw(request) ?: return null
        return json.decodeFromJsonElement(response)
    }
    suspend fun sendRpcRequestRaw(request: RpcRequest<JsonElement>): JsonElement? {
        val id = request.id

        coroutineScope.launch {
            requestsChannel.send(request)
        }

        return responsesFlowSource.first { it.id == id }.result
    }


    private val queueMutex = Mutex()

    /**
     * Subscription function for current filter
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun <T> subscribeWebSocketWithFilter(param: SubscriptionParam<T>): Flow<T> {
        var subscriptionID: String? = null
        return flow {
            val id = queueMutex.withLock { queueID++ }
            val request = RpcRequest(
                id = id,
                method = "eth_subscribe",
                params = buildList {
                    add(json.encodeToJsonElement(param.name))
                    if(param.params != null)
                        add(json.encodeToJsonElement(param.params))
                }
            )
            subscriptionID = sendRpcRequest(request) ?: return@flow

            val responses = responsesFlowSource
                .filter {
                    it.params?.subscription == subscriptionID
                }.mapNotNull {
                    json.decodeFromJsonElement (
                        param.serializer,
                        element = it.params?.result ?: return@mapNotNull null
                    )
                }

            emitAll(responses)
        }.onCompletion {
            val subId = subscriptionID ?: return@onCompletion
            val request = RpcRequest(
                method = "eth_unsubscribe",
                params = listOf(json.encodeToJsonElement(subId))
            )
            sendRpcRequestRaw(request)
        }
    }
}
