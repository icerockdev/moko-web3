package dev.icerock.moko.web3.contract

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class TupleParam(val param: JsonObject) : DynamicEncoder<List<Any>> {
    override fun encode(item: List<Any>): ByteArray {
        val components = param.getValue(key = "components")
            .jsonArray.map(JsonElement::jsonObject)
        return MethodEncoder.encodeParams(components, item)
    }

    override fun decode(source: ByteArray): List<Any> {
        TODO("moko-web3 does not support decoding dynamic params yet")
    }
}
