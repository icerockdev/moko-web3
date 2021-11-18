package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.contract.MethodEncoder.PART_SIZE
import io.ktor.utils.io.core.toByteArray

class StringParam : DynamicEncoder<String> {
    override fun encode(item: String): ByteArray {
        // here we calculate the size of a string
        val size: ByteArray = UInt256Param.encode(item.length.bi)
        return item.chunked(PART_SIZE).map {
            it.toByteArray() + ByteArray(PART_SIZE - it.length)
        }.fold(initial = size) { acc, bytes ->
            acc + bytes
        }
    }

    override fun decode(source: ByteArray): String {
        TODO("moko-web3 does not support decoding dynamic params yet")
    }
}
