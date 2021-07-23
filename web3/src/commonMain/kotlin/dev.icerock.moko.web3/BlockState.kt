package dev.icerock.moko.web3


interface BlockState {
    override fun toString(): String

    object Latest : BlockState {
        override fun toString() = "latest"
    }
    object Earliest : BlockState {
        override fun toString() = "earliest"
    }
    object Pending : BlockState {
        override fun toString() = "pending"
    }

    class Quantity(private val blockNumber: String) : BlockState {
        override fun toString() = blockNumber
    }
}
