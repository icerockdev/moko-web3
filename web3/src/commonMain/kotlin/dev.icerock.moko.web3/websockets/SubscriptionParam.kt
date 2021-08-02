package dev.icerock.moko.web3.websockets

import dev.icerock.moko.web3.WalletAddress


sealed interface SubscriptionParam {
    val name: String
//    val additionalParams: Any? get() = null

    /**
     * Subscribing to this, fires a notification each time a new header is appended to the chain,
     * including chain reorganizations. In case of a chain reorganization the subscription
     * will emit all new headers for the new chain.
     * Therefore the subscription can emit multiple headers on the same height.
     */
    object NewHeads : SubscriptionParam {
        override val name: String = "newHeads"
    }

    /**
     * Returns logs that are included in new imported blocks and match the given filter criteria.
     * In case of a chain reorganization previous sent logs that are on the old chain will be
     * resend with the removed property set to true. Logs from transactions that ended
     * up in the new chain are emitted.
     * Therefore a subscription can emit logs for the same transaction multiple times.
     */
    // TODO
//    sealed class Logs(
//        private val addresses: List<WalletAddress> = listOf(),
//        private val topics: List<String> = listOf()
//    ) : SubscriptionParam {
//        override val name: String = "logs"
//        override val additionalParams: Any? = TODO()
//        companion object : Logs()
//    }

    /**
     * Returns the hash for all transactions that are added to the pending state and are signed
     * with a key that is available in the node. When a transaction that was previously part of
     * the canonical chain isn't part of the new canonical chain after
     * a reogranization its again emitted.
     */
    object NewPendingTransactions : SubscriptionParam {
        override val name: String = "newPendingTransactions"
    }

    /**
     * Indicates when the node starts or stops synchronizing.
     * The result can either be a boolean indicating that the synchronization has started (true),
     * finished (false) or an object with various progress indicators. NOT SUPPORTED ON KOVAN!
     */
    object Syncing : SubscriptionParam {
        override val name: String = "syncing"
    }
}
