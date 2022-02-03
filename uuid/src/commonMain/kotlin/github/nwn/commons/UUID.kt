package github.nwn.commons

import kotlinx.datetime.*
import kotlin.jvm.Synchronized
import kotlin.random.Random
import kotlin.random.nextULong


interface UUIDStableStoreHandler {
    fun update(op: (UUIDBuildState?) -> UUIDBuildState): UUIDBuildState
}

interface UUIDNameHasher {
    fun hash(namespace: UUID, name: String, charset: String): ByteArray
}

expect object SHA1UUIDNameHasher : UUIDNameHasher
expect object MD5UUIDNameHasher : UUIDNameHasher


open class MemoryUUIDStableStoreHandler : UUIDStableStoreHandler {
    protected var state: UUIDBuildState? = null

    @Synchronized
    override fun update(op: (UUIDBuildState?) -> UUIDBuildState): UUIDBuildState {
        val state = op(state)
        this.state = state
        return state
    }
}

expect open class FileUUIDStableStoreHandler : UUIDStableStoreHandler
expect object DefaultFileUUIDStableStoreHandler : FileUUIDStableStoreHandler

object DefaultMemoryUUIDStableStoreHandler : MemoryUUIDStableStoreHandler()

data class UUID(internal val high: ULong, internal val low: ULong) : Comparable<UUID> {

    internal val timeLow: UInt
        get() = ((low shr 32) and 0xFFFFFFFFUL).toUInt()
    internal val timeMid: UShort
        get() = ((low shr 16) and 0xFFFFUL).toUShort()
    internal val timeHighAndVersion: UShort
        get() = (low and 0xFFFFUL).toUShort()
    internal val clockSequenceHighAndReserved: UByte
        get() = ((high shr 56) and 0xFFUL).toUByte()
    internal val clockSequenceLow: UByte
        get() = ((high shr 48) and 0xFFUL).toUByte()
    internal val node: ULong
        get() = (high and 0xFFFFFFFFFFFFUL)


    internal object Version4 {
        const val version: Byte = 0b0100
        fun newUUID(random: Random): UUID {
            val low =
                ((version.toUByte().toULong() and 0b1111UL) shl 12) or (random.nextULong() and 0xFFFFFFFFFFFF0FFFUL)
            val high = RESERVED or (random.nextULong() and 0xFFFFFFFFFFFF3FFFUL)
            return UUID(high, low)
        }
    }

    internal object Version3 {
        const val version: Byte = 0b0011
        fun newUUID(namespace: UUID, name: String, charset: String): UUID {
            var (low, high) = MD5UUIDNameHasher.hash(namespace, name, charset).toULongPair()
            low = ((version.toUByte().toULong() and 0b1111UL) shl 12) or (low and 0xFFFFFFFFFFFF0FFFUL)
            high = RESERVED or (high and 0xFFFFFFFFFFFF3FFFUL)
            return UUID(high, low)
        }
    }

    internal object Version5 {
        const val version: Byte = 0b0101
        fun newUUID(namespace: UUID, name: String, charset: String): UUID {
            var (low, high) = SHA1UUIDNameHasher.hash(namespace, name, charset).toULongPair()
            low = ((version.toUByte().toULong() and 0b1111UL) shl 12) or (low and 0xFFFFFFFFFFFF0FFFUL)
            high = RESERVED or (high and 0xFFFFFFFFFFFF3FFFUL)
            return UUID(high, low)
        }
    }

    internal object Version1 {
        private val gregorianReformEpoch = LocalDateTime(1582, 10, 15, 0, 0, 0, 0).toInstant(UtcOffset.ZERO)
        private const val NANO = 1000000UL
        const val version: Byte = 0b0001
        fun newUUID(stableStoreHandler: UUIDStableStoreHandler): UUID {
            return stableStoreHandler.update { state ->
                val currentInstant = Clock.System.now()
                /* Default minus operation returns the nanoseconds in as a signed 64 bit integer.
                As a result, this causes an overflow because of the large timespan. Since the Gregorian Reform Epoch's
                nanoseconds are always 0, the actual time can be computed by getting the difference in milliseconds,
                converting it to nanoseconds, and adding the nanoseconds of the currentInstant.
                */
                val timestamp =
                    (currentInstant - gregorianReformEpoch).let { diff ->
                        diff.inWholeMilliseconds.toULong() * NANO + ((currentInstant.nanosecondsOfSecond.toULong() / 100UL) * 100UL)
                    }
                val address = getMACAddress()
                val node = address.foldIndexed(0UL) { index, acc, byte ->
                    acc or (byte.toUByte().toULong() shl (index * 8))
                } and 0xFFFFFFFFFFFFUL

                val clockSequence = when {
                    state == null || state.nodeId != node -> {
                        (Random.nextInt() and 0x3FFF).toShort()
                    }
                    state.timestamp > timestamp -> {
                        ((state.clockSequence + 1) and 0x3FFF).toShort()
                    }
                    else -> state.clockSequence
                }
                UUIDBuildState(timestamp, clockSequence, node)
            }.buildUUID(this.version)
        }


    }

    companion object {
        val Nil = UUID(0UL, 0UL)
        val DNS = UUID(0x80b400c04fd430c8UL, 0x6ba7b8109dad11d1UL)
        val X500 = UUID(0x80b400c04fd430c8UL, 0x6ba7b8149dad11d1UL)
        val URL = UUID(0x80b400c04fd430c8UL, 0x6ba7b8119dad11d1UL)
        val OID = UUID(0x80b400c04fd430c8UL, 0x6ba7b8129dad11d1UL)
        internal const val RESERVED: ULong = 0x8000000000000000UL

        /**
         * Generates a Time-Based [UUID] following the guidelines outlined in
         * [RC4122 Section 4.2](https://datatracker.ietf.org/doc/html/rfc4122#section-4.2).
         * @param stableStoreHandler A [UUIDStableStoreHandler] responsible for reading and writing the [UUID] state.
         * By default, the state is stored in a file that is determined by the platform. This behaviour can be
         * changed to store it in memory using [DefaultMemoryUUIDStableStoreHandler], or by implementing a custom
         * [UUIDStableStoreHandler]
         * @return A new [UUID]. As specified in [RC4122 Section 4.2](https://datatracker.ietf
         * .org/doc/html/rfc4122#section-4.2), this UUID is generated using the current timestamp, a clock sequence
         * variable, and the IEEE address. This version trends towards using the localhost, and the IEEE address can
         * be traced back to the source machine from this UUID.
         */
        fun newTimeUUID(stableStoreHandler: UUIDStableStoreHandler = DefaultFileUUIDStableStoreHandler) =
            Version1.newUUID(stableStoreHandler)

        /**
         * Generates a Random [UUID] following the guidelines outlined in
         * [RC4122 Section 4.4](https://datatracker.ietf.org/doc/html/rfc4122#section-4.4).
         * @param random The [Random] instance used for generating random numbers.
         * @return A new [UUID]. As specified in [RC4122 Section 4.4](https://datatracker.ietf
         * .org/doc/html/rfc4122#section-4.4), this UUID is largely just a glorified pseudorandom number. The UUID
         * generated by this method can be compared to that of a 122 bit pseudorandom integer. The odds of collision
         * with this generation method are approximately 1 in 1 billion, but this method is significantly faster than
         * [newTimeUUID]. When traceability isn't required, this should be the preferred method of generating UUIDs.
         */
        fun newRandomUUID(random: Random = Random) = Version4.newUUID(random)

        /**
         * Generates a Name-Based [UUID] following the guidelines outlined in
         * [RC4122 Section 4.3](https://datatracker.ietf.org/doc/html/rfc4122#section-4.3).
         * @param namespace The namespace [UUID]. This UUID is prepended with the bytes from the [name] parameter
         * when hashing. There are predefined namespaces for common usages, such as URLs. A custom namespace UUID can
         * be used in place of those if desired.
         * @param name The name to be hashed for the UUID. This parameter is turned into a [ByteArray] based on the
         * [charset] specified.
         * @param charset The charset to use when turning [name] into a [ByteArray]
         * @return a new [UUID]. As specified in [RC4122 Section 4.3](https://datatracker.ietf
         * .org/doc/html/rfc4122#section-4.3), this UUID utilizes cryptographic hashes of byte arrays to generate
         * [UUIDs][UUID]. The hashing algorithm for this implementation uses SHA-1.
         */
        fun newNameUUID(namespace: UUID, name: String, charset: String = "UTF-8"): UUID =
            Version5.newUUID(namespace, name, charset)

        /**
         * Parses a [UUID] string into a UUID.
         * @param uuid The UUID String.
         * @return A [UUID]
         * @throws IllegalArgumentException If [uuid] is not in the format of AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE
         */
        fun fromString(uuid: String): UUID = fromByteArray(UUIDParser.parse(uuid.trim()))
    }

    override fun toString(): String {
        return "${timeLow.toString(16).padStart(8, '0')}-${
            timeMid.toString(16).padStart(4, '0')
        }-${timeHighAndVersion.toString(16).padStart(4, '0')}-${
            clockSequenceHighAndReserved.toString(16).padStart(2, '0')
        }${clockSequenceLow.toString(16).padStart(2, '0')}-${node.toString(16).padStart(12, '0')}"
    }


    override fun compareTo(other: UUID): Int {
        return high.compareTo(other.high).let { highComparison ->
            if (highComparison == 0)
                low.compareTo(other.low)
            else
                highComparison
        }
    }


}

/**
 * Encodes a [UUID] into a [ByteArray] in big-endian format.
 */
expect fun UUID.toByteArray(): ByteArray

/**
 * Decodes a [UUID] from a [ByteArray] in big-endian format.
 */
expect fun UUID.Companion.fromByteArray(byteArray: ByteArray): UUID

