package dev.icerock.moko.web3.crypto

/**
 * Explanation: https://coincodex.com/article/2078/ethereum-address-checksum-explained/
 * Algorithm: https://ethereum.stackexchange.com/questions/1374/how-can-i-check-if-an-ethereum-address-is-valid,
 *  https://github.com/ethers-io/ethers.js/blob/ce8f1e4015c0f27bf178238770b1325136e3351a/packages/address/src.ts/index.ts#L12
 */
internal fun String.toChecksummedAddress(): String {
    val addressValue = this
        .lowercase()
        .removePrefix(prefix = "0x")

    val hashed = addressValue
        .keccakHash
        .asHexInts

    val result = addressValue
        .mapIndexed { i, char -> char.takeIf { hashed[i] < 8 } ?: char.uppercase() }
        .joinToString(separator = "")

    return "0x$result"
}

private val String.keccakHash get(): ByteArray = this
    .map { it.code.toByte() }
    .toByteArray()
    .let { Keccak.digest(it, KeccakParameter.KECCAK_256) }

// Split the every byte to 2 numbers as
// If it would be hex representation (byte 255 is ff, 15 and 15)
private val ByteArray.asHexInts get(): List<Int> =
    flatMap { byte ->
        with(byte.toUByte().toInt()) {
            listOf(
                // (FA) shr 4 = (1111 1010) shr 4 -> 1111 = f = 15
                shr(bitCount = 4),
                // (FA) and (0x0f) = (1111 1010) and (0000 ffff) -> 1010 = a = 10
                and(other = 0x0f)
            )
        }
    }
