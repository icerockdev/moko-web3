/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal data class BatchRPC(
    val requestInfoChannel: Channel<RequestInfo>,
    var nextRequestId: Int = 0
) : AbstractCoroutineContextElement(BatchRPC) {
    companion object Key : CoroutineContext.Key<BatchRPC>

    data class RequestInfo(
        val id: Int,
        val requestJson: JsonElement,
        val resultChannel: Channel<JsonElement>
    )
}
