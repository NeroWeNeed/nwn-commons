package github.nwn.commons

private val PNG_FILE_HEADER = ubyteArrayOf(0x89u, 0x50u, 0x4eu, 0x47u, 0x0du, 0x0au, 0x1au, 0x0au)
private val WEBP_FILE_HEADER_P1 =
    ubyteArrayOf('R'.code.toUByte(), 'I'.code.toUByte(), 'F'.code.toUByte(), 'F'.code.toUByte())
private val WEBP_FILE_HEADER_P2 =
    ubyteArrayOf('W'.code.toUByte(), 'E'.code.toUByte(), 'B'.code.toUByte(), 'P'.code.toUByte())
private val JPEG_FILE_SOI = ubyteArrayOf(0xFFu, 0xD8u)
private val GIF_FILE_HEADER_1 = ubyteArrayOf('G'.code.toUByte(),'I'.code.toUByte(),'F'.code.toUByte(),'8'.code.toUByte(),'9'.code.toUByte(),'a'.code.toUByte())
private val GIF_FILE_HEADER_2 = ubyteArrayOf('G'.code.toUByte(),'I'.code.toUByte(),'F'.code.toUByte(),'8'.code.toUByte(),'7'.code.toUByte(),'a'.code.toUByte())
private val AVIF_HEADER = ubyteArrayOf('f'.code.toUByte(),'t'.code.toUByte(),'y'.code.toUByte(),'p'.code.toUByte(),'a'.code.toUByte(),'v'.code.toUByte(),'i'.code.toUByte(),'f'.code.toUByte())
fun MimeTypeParserSetBuilder.imageTypes() {
    mimeType(MimeType(Category.Image, "png")) {
        extension("png")
        header(8) {
            it.toUByteArray().contentEquals(PNG_FILE_HEADER)
        }
    }
    mimeType(MimeType(Category.Image, "webp")) {
        extension("webp")
        header(12) { headerBytes ->
            val bytes = headerBytes.toUByteArray()
            (0 until 4).forEach {
                if (bytes[it] != WEBP_FILE_HEADER_P1[it])
                    return@header false
            }
            (8 until 12).forEach {
                if (bytes[it] != WEBP_FILE_HEADER_P2[it])
                    return@header false
            }
            true
        }
    }
    mimeType(MimeType(Category.Image, "jpeg")) {
        extension("jpeg")
        extension("jpg")
        extension("jfif")
        extension("jpe")
        extension("jif")
        header(2) {
            it.toUByteArray().contentEquals(JPEG_FILE_SOI)
        }
    }
    mimeType(MimeType(Category.Image,"apng")) {
        extension("apng")
        header(8) {
            it.toUByteArray().contentEquals(PNG_FILE_HEADER)
        }
    }
    mimeType(MimeType(Category.Image,"gif")) {
        extension("gif")
        header(6) {
            val bytes = it.toUByteArray()
            bytes.contentEquals(GIF_FILE_HEADER_1) || bytes.contentEquals(GIF_FILE_HEADER_2)
        }
    }
    mimeType(MimeType(Category.Image,"avif")) {
        extension("avif")
        header(12) {
            val x = UByteArray(8) { 0u }
            it.toUByteArray().copyInto(x,0,4)
            x.contentEquals(AVIF_HEADER)
        }
    }
}