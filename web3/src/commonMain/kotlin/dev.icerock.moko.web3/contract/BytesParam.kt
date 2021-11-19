package dev.icerock.moko.web3.contract

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.contract.MethodEncoder.PART_SIZE

object BytesParam : DynamicEncoder<ByteArray> {
    override fun encode(item: ByteArray): ByteArray {
        val sizeData = UInt256Param.encode(item.size.bi)
        return item
            .asIterable()
            .chunked(PART_SIZE)
            .map {
                it.toByteArray() + ByteArray(PART_SIZE - it.size)
            }.fold(initial = sizeData) { acc, bytes ->
                acc + bytes
            }
    }

    override fun decode(source: ByteArray): ByteArray {
        TODO("moko-web3 does not support decoding dynamic params yet")
    }
}