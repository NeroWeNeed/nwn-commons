package github.nwn.commons

class MimeTypeParserSetBuilder {
    private val items = ArrayList<MimeTypeParser>()
    fun mimeType(mimeType: MimeType, op: MimeTypeParserBuilder.() -> Unit) {
        items.add(MimeTypeParserBuilder(mimeType).apply(op).build())
    }
    internal fun build() = SimpleMimeTypeSet(items)

}
fun mimeTypes(op: MimeTypeParserSetBuilder.() -> Unit) = MimeTypeParserSetBuilder().apply(op).build()
class MimeTypeParserBuilder(private val mimeType: MimeType) {
    private val extensions = HashSet<String>()
    private var headerValidator: HeaderValidator? = null
    fun extension(ext: String) {
        extensions.add(ext)
    }

    fun header(byteArrayRequestSize: Int, op: (ByteArray) -> Boolean) {
        headerValidator = HeaderValidator(byteArrayRequestSize, op)
    }

    fun build() = MimeTypeParser(mimeType, extensions, headerValidator)
}

data class MimeTypeParser(
    val mimeType: MimeType,
    val fileExtensions: Set<String>,
    val headerValidator: HeaderValidator?
)

data class HeaderValidator(val byteRequestSize: Int,private val op: (ByteArray) -> Boolean) {
    operator fun invoke(byteArray: ByteArray) = op(byteArray)
}