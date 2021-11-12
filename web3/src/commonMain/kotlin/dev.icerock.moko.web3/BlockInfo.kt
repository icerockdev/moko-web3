/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

@file:UseSerializers(BigIntSerializer::class)

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
import kotlinx.serialization.UseSerializers

@Serializable
data class BlockInfo(
    val number: BigInt?, // null if the block is pending
    val hash: BlockHash,
    val parentHash: Hex32String,
    val nonce: Hex8String,
    val logsBloom: Hex256String,
    val transactionsRoot: Hex32String,
    val stateRoot: Hex32String,
    val miner: WalletAddress,
    val difficulty: BigInt,
    val totalDifficulty: BigInt,
    val extraData: String,
    val size: BigInt,
    val gasLimit: BigInt,
    val gasUsed: BigInt,
    val timestamp: BigInt,
    val transactions: List<TransactionInfo>,
    val uncles: List<BlockHash>
)
