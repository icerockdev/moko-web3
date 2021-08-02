/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.entity.InfuraRequest
import dev.icerock.moko.web3.entity.Web3SocketResponse
import dev.icerock.moko.web3.websockets.SubscriptionParam
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Class to work with webSocket connection in Etherium network
 * @param httpClient client to work with WebSocket
 */
class Web3Socket(
    private val httpClient: HttpClient,
    private val json: Json,
    private val webSocketUrl: String,
    private val coroutineScope: CoroutineScope
) {

    /**
     * channel to receive data from webSocket
     */
    private val responsesFlowSource: MutableSharedFlow<Web3SocketResponse> =
        MutableSharedFlow()

    val responsesFlow: SharedFlow<Web3SocketResponse> = responsesFlowSource.asSharedFlow()

    /**
     * subscription filter's flow, here we emit new
     */
    private val requestsFlow: MutableSharedFlow<InfuraRequest<String>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    /**
     * incremental field to filter different incoming messages from websocket
     */
    private var queueID: Int = 0

    init {
        // launch websocket connection to work with in over web3Socket lifecycle
        coroutineScope.launch {
            httpClient.webSocket(webSocketUrl) {
                requestsFlow
                    .map { request ->
                        json.encodeToString(
                            serializer = InfuraRequest.serializer(String.serializer()),
                            value = request
                        )
                    }
                    .map { encoded -> Frame.Text(encoded) }
                    .onEach { frame -> outgoing.send(frame) }
                    .launchIn(this)

                incoming
                    .consumeAsFlow()
                    .mapNotNull { frame ->
                        val textFrame = frame as? Frame.Text
                        return@mapNotNull textFrame?.readText()
                    }
                    .map { text ->
                        json.decodeFromString(
                            deserializer = Web3SocketResponse.serializer(),
                            string = text
                        )
                    }
                    .collect {
                        responsesFlowSource.emit(it)
                    }
            }
        }
    }

    suspend fun sendRpcRequest(request: InfuraRequest<String>): String? {
        val id = request.id

        coroutineScope.launch {
            requestsFlow.emit(request)
        }

        return responsesFlowSource.first { it.id == id }.result
    }


    private val queueMutex = Mutex()

    /**
     * Subscription function for current filter
     * @param params parameters for infura subscription
     */
    fun subscribeWebSocketWithFilter(params: List<SubscriptionParam>): Flow<String> {
        var subscriptionID: String? = null
        return flow {
            val id = queueMutex.withLock { queueID++ }
            val request = InfuraRequest(
                id = id,
                method = "eth_subscribe",
                params = params.map(SubscriptionParam::name)
            )
            subscriptionID = sendRpcRequest(request) ?: return@flow

            responsesFlowSource
                .filter {
                    it.params?.subscription == subscriptionID
                }.mapNotNull {
                    it.params?.result
                }.collect(this::emit)
        }.onCompletion {
            val subId = subscriptionID ?: return@onCompletion
            val request = InfuraRequest(
                method = "eth_unsubscribe",
                params = listOf(subId)
            )
            sendRpcRequest(request)
        }
    }

    fun subscribeWebSocketWithFilter(vararg filters: SubscriptionParam) =
        subscribeWebSocketWithFilter(filters.toList())
}