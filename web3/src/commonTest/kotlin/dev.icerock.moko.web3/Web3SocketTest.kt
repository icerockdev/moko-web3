package dev.icerock.moko.web3

import dev.icerock.moko.web3.websockets.createHttpClientEngine
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class Web3SocketTest {

    private lateinit var web3Socket: Web3Socket

    @BeforeTest
    fun `create_socket`() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val httpClient = HttpClient(createHttpClientEngine()) {
            install(WebSockets) {

            }
        }

        web3Socket = Web3Socket(
            httpClient = httpClient,
            json = json,
            webSocketUrl = "wss://rinkeby.infura.io/ws/v3/59d7fae3226b40e09d84d713e588305b",
            coroutineScope = GlobalScope
        )
    }

    @Test
    fun `test_web_socket_flow`() {
        val subcriptionFilterTransactions: List<String> = listOf("newPendingTransactions")
        runBlocking {
            web3Socket.subscribeWebSocketWithFilter(subcriptionFilterTransactions)
                .onEach {
                    println(it)
                }
                .onEmpty {
                    println("flow is empty")
                }
                .take(2)
                .collect {
                    println("received on inherited channel: $it")
                }

            val subscriptionFilterHeads: List<String> = listOf("newHeads")

            web3Socket.subscribeWebSocketWithFilter(subscriptionFilterHeads)
                .onEach {
                    println(it)
                }
                .onEmpty {
                    println("flow is empty")
                }
                .take(2)
                .collect {
                    println("$it received on inherited channel")
                }
        }
    }
}