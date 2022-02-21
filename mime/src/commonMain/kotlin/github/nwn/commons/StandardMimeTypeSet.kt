package github.nwn.commons

object StandardMimeTypeSet : MimeTypeSet() {
    private val mimeTypes = mimeTypes {
        imageTypes()
    }
    override val mimeTypeParsers: List<MimeTypeParser>
        get() = mimeTypes.mimeTypeParserInternal
    override val maxByteRequestSize: Int
        get() = mimeTypes.maxByteRequestSizeInternal
    override val categories: List<Category>
        get() = mimeTypes.categories

    override fun get(type: Category): List<MimeType> = mimeTypes[type]

    override fun get(mime: String): MimeType = mimeTypes[mime]

}