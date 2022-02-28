package github.nwn.commons

import java.io.File
actual fun MimeTypeSet.parse(url: Url): MimeType? {
    if (url.scheme == "file")
        return parse(url.toFile())
    val ext = url.path.value.substringAfterLast('.')
    return mimeTypeParserInternal.firstOrNull { it.fileExtensions.contains(ext) }?.mimeType
}



fun MimeTypeSet.parse(file: File) : MimeType? {
    val ext = file.extension

    val initial = if (maxByteRequestSizeInternal > 0) {
        file.inputStream().use {
            it.readNBytes(maxByteRequestSizeInternal)
        }.let {
            if (it.isNotEmpty())
                it
            else
                null
        }
    } else {
        null
    }
    return mimeTypeParserInternal.firstOrNull { mimeTypeParser ->
        if (mimeTypeParser.fileExtensions.contains(ext)) {
            if (mimeTypeParser.headerValidator != null && initial != null) {
                if (initial.size < mimeTypeParser.headerValidator.byteRequestSize)
                    return@firstOrNull false
                val bytes = initial.sliceArray(0 until mimeTypeParser.headerValidator.byteRequestSize)
                mimeTypeParser.headerValidator.invoke(bytes)
            }
            else {
                true
            }
        }
        else
            false
    }?.mimeType ?: run {
        mimeTypeParserInternal.firstOrNull { mimeTypeParser ->
            if (mimeTypeParser.headerValidator != null && initial != null) {
                if (initial.size < mimeTypeParser.headerValidator.byteRequestSize)
                    return@firstOrNull false
                val bytes = initial.sliceArray(0 until mimeTypeParser.headerValidator.byteRequestSize)
                mimeTypeParser.headerValidator.invoke(bytes)
            }
            else {
                false
            }
        }
    }?.mimeType
}