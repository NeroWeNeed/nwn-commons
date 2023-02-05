package github.nwn.commons

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable(with = MimeType.Serializer::class)
data class MimeType(val type: Category, val subtype: String) {

    object Serializer : KSerializer<MimeType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("github.nwn.commons.MimeType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MimeType {
            val (category, subType) = decoder.decodeString().split('/')
            return MimeType(Category[category], subType)
        }

        override fun serialize(encoder: Encoder, value: MimeType) {
            encoder.encodeString(value.toString())
        }

    }

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

@Serializable
sealed class Category(val name: String) {
    companion object {
        private val categories by lazy {
            mapOf<String, Category>(
                Image.name to Image,
                Application.name to Application,
                Audio.name to Audio,
                Font.name to Font,
                Model.name to Model,
                Text.name to Text,
                Example.name to Example,
                Video.name to Video,
            )
        }

        operator fun get(name: String): Category = categories.getValue(name)
    }

    object Image : Category("image")
    object Application : Category("application")
    object Audio : Category("audio")
    object Font : Category("font")
    object Model : Category("model")
    object Text : Category("text")
    object Example : Category("example")
    object Video : Category("video")

}


