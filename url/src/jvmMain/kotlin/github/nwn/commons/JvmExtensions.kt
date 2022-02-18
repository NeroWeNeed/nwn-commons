package github.nwn.commons

import java.io.File
import java.net.URL

fun Url.toFile(): File =
    if (this.scheme == FILE_SCHEME_NAME) File(
        this.path.value.replace('/', File.pathSeparatorChar)
            .percentDecoded()
    ) else throw IllegalArgumentException(
        "Url scheme must be $FILE_SCHEME_NAME"
    )

fun Url.toJVM(): URL = URL(this.toString())
fun File.toUrl() = Url("${FILE_SCHEME_NAME}:${this.path.replace(File.pathSeparatorChar,'/')}")