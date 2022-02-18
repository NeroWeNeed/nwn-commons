package github.nwn.commons

import java.io.ByteArrayOutputStream

private const val digitMap = "0123456789ABCDEF"

actual fun String.percentDecoded(): String {
    return ByteArrayOutputStream().use {
        var index = 0

        while (index < this.length) {
            val byte = this[index]
            index += if (byte == '%') {
                it.write(
                    (digitMap.indexOf(this[index + 1].uppercaseChar()) * 16 + digitMap.indexOf(
                        this[index + 2]
                            .uppercaseChar()
                    ))
                )
                3
            } else {
                it.write(byte.code.and(0xFF))
                1
            }
        }
        String(it.toByteArray(),Charsets.UTF_8)
    }

}

private val UNRESERVED = intArrayOf(
    0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A,
    0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A,
    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
    0x2D, 0x2E, 0x5F, 0x7E,
    0x2F // Path Character
)

actual fun String.percentEncoded(): String {
    val bytes = this.encodeToByteArray()
    val sb = StringBuilder()
    bytes.forEach {
        val s = it.toUByte().toInt()
        if (UNRESERVED.contains(s))
            sb.append(s.toChar())
        else
            sb.append("%${s.toString(16).uppercase()}")
    }
    return sb.toString()
}
