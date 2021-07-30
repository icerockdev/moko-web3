/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import dev.icerock.moko.web3.entity.InfuraRequest
import dev.icerock.moko.web3.entity.Web3SocketResponse
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Class to work with webSocket connection in Etherium network
 * @param httpClient client to work with WebSocket
 */
class Web3Socket(
    private val httpClient: HttpClient,
    private val json: Json,
    private val webSocketUrl: String,
    coroutineScope: CoroutineScope
) {

    /**
     * channel to receive data from webSocket
     */
    private val webSocketsSharedFlow: MutableSharedFlow<Web3SocketResponse> =
        MutableSharedFlow(onBufferOverflow = BufferOverflow.SUSPEND)

    /**
     * subscription filter's flow, here we emit new
     */
    private val subscriptionFlow: MutableSharedFlow<InfuraRequest<String>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)

    /**
     * incremental field to filter different incoming messages from websocket
     */
    private var queueID: Int = 0

    init {
        // launch websocket connection to work with in over web3Socket lifecycle
        coroutineScope.launch {
            httpClient.webSocket(webSocketUrl) {
                println("connection established!")
                // when connection is established, we are going to map all the incoming messages to flow
                val job = incoming
                    .consumeAsFlow()
                    .mapNotNull {
                        (it as? Frame.Text)?.readText()
                    }
                    .map {
                        json.decodeFromString(
                            deserializer = Web3SocketResponse.serializer(),
                            string = it
                        )
                    }
                    .onEach {
                        println("incoming message:  $it")
                        webSocketsSharedFlow.emit(it)
                    }
                    .launchIn(this)
                println("incoming messages setup finished")

                subscriptionFlow
                    .map {
                        json.encodeToString(
                            serializer = InfuraRequest.serializer(String.serializer()),
                            value = it
                        )
                    }
                    .map {
                        Frame.Text(it)
                    }.onEach {
                        outgoing.send(it)
                    }
                    .launchIn(this)
                println("subscription flow setup finished")
                job.join()
            }
            println("websocket closed")
        }
    }

    /**
     * Subscription function for current filter
     * @param params parameters for infura subscription
     */
    fun subscribeWebSocketWithFilter(params: List<String>): Flow<String> {
        var subscriptionID: String? = null
        return flow {
            subscriptionID = coroutineScope {
                // for new subscription we increment queueID value
                queueID++
                // waiting for a first message that return request with id == queueID
                val deferred = async {
                    // Async call to filter from websocket by id that was inceremented
                    println("start filtering by id=$queueID")
                    webSocketsSharedFlow
                        .filter {
                            it.id == queueID
                        }
                        .mapNotNull {
                            it.result
                        }
                        .first()
                }
                // send request to web socket to subscribe on filter
                InfuraRequest(
                    id = queueID,
                    method = "eth_subscribe",
                    params = params
                ).also { request ->
                    subscriptionFlow.emit(request)
                }
                // waiting for async result
                println("waiting for filter result...")
                deferred.await()
            }

            // after we've got a subscriptionID, we can filter incoming messages by it
            webSocketsSharedFlow
                .filter {
                    it.params?.subscription == subscriptionID
                }.mapNotNull {
                    it.params?.result
                }.onEach {
                    emit(it)
                }
                .collect()
        }
            .onCompletion {
            // unsubscribe by subscriptionID
            val subId = subscriptionID ?: return@onCompletion
            InfuraRequest(
                method = "eth_unsubscribe",
                params = listOf(subId)
            ).also { request ->
                subscriptionFlow.emit(request)
            }
            println("$subId unsubscribed!")
        }
    }
}