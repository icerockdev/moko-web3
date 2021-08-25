package dev.icerock.moko.web3.crypto

import dev.icerock.moko.web3.ContractAddress
import kotlin.test.Test
import kotlin.test.assertEquals

class ChecksummedAddressTest {
    @Test
    fun `test to checksummed address`() {
        val contractAddress = ContractAddress(value = "0xC9472182861faD74b6810cf58C792501F8837473").also(::println)
        assertEquals(
            expected = contractAddress,
            actual = contractAddress.checksummed
        )
    }
}
