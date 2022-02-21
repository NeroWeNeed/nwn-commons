package github.nwn.commons

data class MimeType(val type: Category, val subtype: String) {
    override fun toString(): String {
        return "${type.name}/$subtype"
    }
}

abstract class MimeTypeSet {
    protected abstract val mimeTypeParsers: List<MimeTypeParser>
    protected abstract val maxByteRequestSize: Int
    abstract val categories: List<Category>
    abstract operator fun get(type: Category): List<MimeType>
    abstract operator fun get(mime: String): MimeType
    internal val mimeTypeParserInternal: List<MimeTypeParser>
        get() = mimeTypeParsers
    internal val maxByteRequestSizeInternal: Int
    get() = maxByteRequestSize
}

open class SimpleMimeTypeSet(mimeTypeParsers: Iterable<MimeTypeParser>) : MimeTypeSet() {
    override val mimeTypeParsers = mimeTypeParsers.toList()
    override val maxByteRequestSize: Int = mimeTypeParserInternal.maxOf { it.headerValidator?.byteRequestSize ?: 0 }
    private val mimeTypes = mimeTypeParsers.associate { it.mimeType.toString() to it.mimeType }
    override val categories = mimeTypes.values.map { it.type }.distinct()
    override operator fun get(type: Category) = mimeTypes.values.filter { it.type == type }
    override operator fun get(mime: String): MimeType =
        mimeTypes[mime] ?: throw IllegalArgumentException("Unknown Mime Type: $mime")
}

expect fun MimeTypeSet.parse(url: Url): MimeType?


sealed class Category(val name: String) {
    object Image : Category("image")
    object Application : Category("application")
    object Audio : Category("audio")
    object Font : Category("font")
    object Model : Category("model")
    object Text : Category("text")
    object Example : Category("example")
    object Video : Category("video")

}


