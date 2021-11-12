/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.web3

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex256String
import dev.icerock.moko.web3.hex.Hex256StringSerializer
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.Hex32StringSerializer
import dev.icerock.moko.web3.hex.Hex8String
import dev.icerock.moko.web3.hex.Hex8StringSerializer
import dev.icerock.moko.web3.serializer.BigIntSerializer
import kotlinx.serialization.Serializable

@Serializable
data class BlockInfo(
    @Serializable(with = BigIntSerializer::class)
    val number: BigInt?, // null if the block is pending
    @Serializable(with = BlockHashSerializer::class)
    val hash: BlockHash,
    @Serializable(with = Hex32StringSerializer::class)
    val parentHash: Hex32String,
    @Serializable(with = Hex8StringSerializer::class)
    val nonce: Hex8String,
    @Serializable(with = Hex256StringSerializer::class)
    val logsBloom: Hex256String,
    @Serializable(with = Hex32StringSerializer::class)
    val transactionsRoot: Hex32String,
    @Serializable(with = Hex32StringSerializer::class)
    val stateRoot: Hex32String,
    @Serializable(with = WalletAddressSerializer::class)
    val miner: WalletAddress,
    @Serializable(with = BigIntSerializer::class)
    val difficulty: BigInt,
    @Serializable(with = BigIntSerializer::class)
    val totalDifficulty: BigInt,
    val extraData: String,
    @Serializable(with = BigIntSerializer::class)
    val size: BigInt,
    @Serializable(with = BigIntSerializer::class)
    val gasLimit: BigInt,
    @Serializable(with = BigIntSerializer::class)
    val gasUsed: BigInt,
    @Serializable(with = BigIntSerializer::class)
    val timestamp: BigInt,
    val transactions: List<TransactionInfo>,
    val uncles: List<@Serializable(with = BlockHashSerializer::class) BlockHash>
)
