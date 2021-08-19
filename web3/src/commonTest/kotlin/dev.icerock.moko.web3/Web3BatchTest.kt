/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import io.ktor.client.engine.mock.respondOk
import io.ktor.content.TextContent
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Web3BatchTest {

    @Test
    fun `batch request`() {
        val web3: Web3 = createMockWeb3 { request ->
            val body = request.body
            assertTrue(body is TextContent)

            assertEquals(
                expected = """[{"jsonrpc":"2.0","id":0,"method":"eth_getBalance","params":["0x0001","latest"]},{"jsonrpc":"2.0","id":1,"method":"eth_getBalance","params":["0x0002","latest"]}]""",
                actual = body.text
            )

            respondOk("""[{"jsonrpc": "2.0", "result": "0x12", "id": 1},{"jsonrpc": "2.0", "result": "0x10", "id": 0}]""")
        }

        val response: List<BigInt> = runBlocking {
            web3.batch {
                listOf(
                    async { getEthBalance(WalletAddress("0x0001")) },
                    async { getEthBalance(WalletAddress("0x0002")) }
                )
            }
        }

        assertEquals(
            expected = listOf(
                BigInt(0x10),
                BigInt(0x12)
            ),
            actual = response
        )
    }
}
