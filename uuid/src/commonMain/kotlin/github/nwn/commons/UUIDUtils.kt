package github.nwn.commons

internal expect fun ByteArray.toULongPair(): Pair<ULong, ULong>

internal expect fun getMACAddress(): ByteArray

data class UUIDBuildState(val timestamp: ULong, val clockSequence: Short, val nodeId: ULong) {
    fun buildUUID(version: Byte): UUID {
        val low = ((timestamp and 0xFFFFFFFFFFFF0000UL) shl 16) or ((version.toUByte()
            .toULong() and 0b1111UL) shl 12) or (timestamp and 0xFFFUL)
        val high = nodeId or (clockSequence.toULong() shl 48) or UUID.RESERVED
        return UUID(high, low)
    }
}

internal object UUIDParser {
    private val regex =
        Regex("([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})")
    private val digitMap = mapOf(
        '0' to 0,
        '1' to 1,
        '2' to 2,
        '3' to 3,
        '4' to 4,
        '5' to 5,
        '6' to 6,
        '7' to 7,
        '8' to 8,
        '9' to 9,
        'A' to 10,
        'B' to 11,
        'C' to 12,
        'D' to 13,
        'E' to 14,
        'F' to 15,
        'a' to 10,
        'b' to 11,
        'c' to 12,
        'd' to 13,
        'e' to 14,
        'f' to 15,
    )

    private fun digitValue(char1: Char, char2: Char): Int {
        return digitMap.getValue(char1) * 16 + digitMap.getValue(char2)
    }

    fun parse(str: String): ByteArray {
        return regex.matchEntire(str)?.let {
            parse(
                it.groupValues[1],
                it.groupValues[2],
                it.groupValues[3],
                it.groupValues[4],
                it.groupValues[5]
            )
        } ?: throw IllegalArgumentException("Invalid UUID: $str")
    }

    private fun parse(str1: String, str2: String, str3: String, str4: String, str5: String): ByteArray {
        val ba = ByteArray(16)
        parse(str1, ba, 0)
        parse(str2, ba, 4)
        parse(str3, ba, 6)
        parse(str4, ba, 8)
        parse(str5, ba, 10)
        return ba
    }

    private fun parse(str: String, byteArray: ByteArray, startIndex: Int) {
        (0 until (str.length / 2)).forEachIndexed { index, i ->
            byteArray[startIndex + index] = digitValue(str[i * 2], str[(i * 2) + 1]).toUInt().toUByte().toByte()
        }
    }
}

