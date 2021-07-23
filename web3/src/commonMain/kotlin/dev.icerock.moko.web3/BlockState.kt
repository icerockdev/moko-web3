package dev.icerock.moko.web3

import kotlin.jvm.JvmInline


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

    @JvmInline
    value class Quantity(private val blockNumber: String) : BlockState {
        override fun toString() = blockNumber
    }
}
