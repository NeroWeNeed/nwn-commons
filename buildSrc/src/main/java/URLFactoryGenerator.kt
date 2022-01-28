import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import kotlinx.serialization.*
import kotlinx.serialization.json.*


class URLFactoryGenerator : CodeGenerator {
    companion object {
        val UrlSchemeInfoClass = ClassName("github.nwn.commons", "UrlSchemeInfo")
    }

    override fun invoke(packageName: String, className: String, output: File) {
        val items = Json.decodeFromString<List<UrlSchemeInfo>>(
            URLFactoryGenerator::class.java.getResourceAsStream("scheme_info.json").reader().use { it.readText() })
        val file = FileSpec.builder(packageName, output.nameWithoutExtension).addType(
            factoryBuilder(className, items)
        ).build()
        output.writer().use {
            file.writeTo(it)
        }
    }

    private fun factoryBuilder(className: String, items: List<UrlSchemeInfo>) = TypeSpec.objectBuilder(className)
        .addSuperinterface(ClassName("github.nwn.commons", "UrlFactory"))
        .addProperty(defaultSchemaPortPropertyBuilder(items))
        .build()

    fun defaultSchemaPortPropertyBuilder(items: List<UrlSchemeInfo>) = PropertySpec.builder(
        "schemeInfo",
        Map::class.asClassName().parameterizedBy(String::class.asTypeName(), UrlSchemeInfoClass),
        KModifier.OVERRIDE
    ).initializer(itemMapBuilder(items)).build()

    fun itemMapBuilder(items: List<UrlSchemeInfo>): CodeBlock {
        var t = CodeBlock.builder().add("mapOf(")
        val iter = items.iterator()
        while (iter.hasNext()) {
            t = t.itemBuilder(iter.next())
            if (iter.hasNext())
                t = t.add(",\n")
        }
        return t.add(")").build()
    }

    fun CodeBlock.Builder.itemBuilder(item: UrlSchemeInfo) =
        this.add("%S to %T(%S,%L,%S)", item.name, UrlSchemeInfoClass, item.name, item.port, item.host)
}