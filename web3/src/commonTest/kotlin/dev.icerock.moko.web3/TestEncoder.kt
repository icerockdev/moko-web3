package dev.icerock.moko.web3

import dev.icerock.moko.web3.contract.MethodEncoder
import dev.icerock.moko.web3.hex.internal.toHex
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TestEncoder {

    // Checked, everything works fine
    // to check it, please visit https://abi.hashex.org and place there your abi
    @Test
    fun `test string encoder`() {
        val abiJson = createTestAbi(Json)
        val callData: String = MethodEncoder.createCallData(
            abi = abiJson,
            method = "testStringEncoder",
            params = listOf("test")
        )
        val expected = "0xe4e8653c000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000047465737400000000000000000000000000000000000000000000000000000000"
        assertEquals(expected, callData)
    }

    // Checked, everything works fine
    @Test
    fun `test bytes encoder`() {
        val abiJson = createTestAbi(Json)
        val array = byteArrayOf(0, 11, 22, 33, 44, 55, 66, 77)
        val callData: String = MethodEncoder.createCallData(
            abi = abiJson,
            method = "testBytesEncoder",
            params = listOf(array)
        )
        val expected = "0xe1d2647500000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000008000b16212c37424d000000000000000000000000000000000000000000000000"
        assertEquals(expected, callData)
    }
}
