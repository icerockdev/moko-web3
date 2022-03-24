/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3.websockets

import dev.icerock.moko.web3.NewHeadsWeb3SocketEvent
import dev.icerock.moko.web3.SyncingWeb3SocketEvent
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.entity.LogEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * @param TEvent event returned by websocket
 */
sealed interface SubscriptionParam<TEvent> {
    val name: String
    val params: Map<String, JsonElement>? get() = null
    val serializer: KSerializer<TEvent>

    /**
     * Subscribing to this, fires a notification each time a new header is appended to the chain,
     * including chain reorganizations. In case of a chain reorganization the subscription
     * will emit all new headers for the new chain.
     * Therefore the subscription can emit multiple headers on the same height.
     */
    object NewHeads : SubscriptionParam<NewHeadsWeb3SocketEvent> {
        override val name: String = "newHeads"
        override val serializer: KSerializer<NewHeadsWeb3SocketEvent> = NewHeadsWeb3SocketEvent.serializer()
    }

    /**
     * Returns logs that are included in new imported blocks and match the given filter criteria.
     * In case of a chain reorganization previous sent logs that are on the old chain will be
     * resented with the removed property set to true. Logs from transactions that ended
     * up in the new chain are emitted.
     * Therefore, a subscription can emit logs for the same transaction multiple times.
     */
    sealed class Logs constructor(
        addresses: List<WalletAddress>? = null,
        topics: List<String>? = null,
        @Suppress("UNUSED_PARAMETER")
        unused: Nothing? = null
    ) : SubscriptionParam<LogEvent> {
        final override val name: String = "logs"
        @OptIn(ExperimentalStdlibApi::class)
        final override val params = buildMap<String, JsonElement> {
            if(addresses != null)
                put("address", Json.encodeToJsonElement(addresses.map(WalletAddress::prefixed)))
            if(topics != null)
                put("topics", Json.encodeToJsonElement(topics))
        }
        final override val serializer: KSerializer<LogEvent> = LogEvent.serializer()

        companion object : Logs()
    }

    private class LogsImpl(
        addresses: List<WalletAddress>? = null,
        topics: List<String>? = null
    ) : Logs(addresses, topics)

    fun Logs(
        addresses: List<WalletAddress>? = null,
        topics: List<String>? = null
    ): Logs = LogsImpl(addresses, topics)

    fun Logs(address: WalletAddress) = Logs(listOf(address), listOf())

    /**
     * Returns the hash for all transactions that are added to the pending state and are signed
     * with a key that is available in the node. When a transaction that was previously part of
     * the canonical chain isn't part of the new canonical chain after
     * a reogranization its again emitted.
     */
    object NewPendingTransactions : SubscriptionParam<String> {
        override val name: String = "newPendingTransactions"
        override val serializer: KSerializer<String> = String.serializer()
    }

    /**
     * Indicates when the node starts or stops synchronizing.
     * The result can either be a boolean indicating that the synchronization has started (true),
     * finished (false) or an object with various progress indicators. NOT SUPPORTED ON KOVAN!
     */
    object Syncing : SubscriptionParam<SyncingWeb3SocketEvent> {
        override val name: String = "syncing"
        override val serializer: KSerializer<SyncingWeb3SocketEvent> = SyncingWeb3SocketEvent.serializer()
    }
}
