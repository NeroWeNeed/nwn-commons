package github.nwn.commons

interface UrlFactory {
    val schemeInfo: Map<String, UrlSchemeInfo>
}

data class SimpleUrlFactory(override val schemeInfo: Map<String, UrlSchemeInfo>) : UrlFactory
class UrlFactoryBuilder(factory: UrlFactory? = null) {
    private val defaultSchemePorts: MutableMap<String, UrlSchemeInfo> = HashMap(factory?.schemeInfo ?: emptyMap())
    fun scheme(name: String, port: Int = -1, host: String = "") {
        defaultSchemePorts[name.lowercase()] = UrlSchemeInfo(name, port.coerceIn(-1..PORT_MAX_VALUE), host)
    }

    internal fun build() = SimpleUrlFactory(defaultSchemePorts.toMap())
}

data class Url internal constructor(
    val scheme: String,
    val authority: Authority,
    val path: Path,
    val query: Query,
    val fragment: String,
    private val source: UrlFactory
) {
    companion object {
        operator fun invoke(url: String, factory: UrlFactory = DefaultUrlFactory) = factory.create(url)
    }

    override fun toString(): String {
        return "$scheme:${if (authority.isEmpty()) "" else "//${authority.toString(source.schemeInfo[scheme]?.defaultPort != authority.port)}"}${path}${if (query.isEmpty()) "" else "?${query.value}"}${if (fragment.isEmpty()) "" else "#$fragment"}"
    }
}

private const val URI =
    "([A-Za-z](?:[A-Za-z]|[0-9]|[\\u002B\\u002D\\u002E])*)\\u003A(?:\\u002F{2}(?:((?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A)*\\u0040)?(\\u005B(?:(?:(?:[0-9A-Fa-f]{1,4}\\u003A){6}(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|\\u003A{2}(?:[0-9A-Fa-f]{1,4}\\u003A){5}(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|(?:[0-9A-Fa-f]{1,4})?\\u003A{2}(?:[0-9A-Fa-f]{1,4}\\u003A){4}(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|(?:(?:[0-9A-Fa-f]{1,4}\\u003A)?[0-9A-Fa-f]{1,4})?\\u003A{2}(?:[0-9A-Fa-f]{1,4}\\u003A){3}(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|(?:(?:[0-9A-Fa-f]{1,4}\\u003A){0,2}[0-9A-Fa-f]{1,4})?\\u003A{2}(?:[0-9A-Fa-f]{1,4}\\u003A){2}(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|(?:(?:[0-9A-Fa-f]{1,4}\\u003A){0,3}[0-9A-Fa-f]{1,4})?\\u003A{2}[0-9A-Fa-f]{1,4}\\u003A(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|(?:(?:[0-9A-Fa-f]{1,4}\\u003A){0,4}[0-9A-Fa-f]{1,4})?\\u003A{2}(?:[0-9A-Fa-f]{1,4}\\u003A[0-9A-Fa-f]{1,4}|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035]))|(?:(?:[0-9A-Fa-f]{1,4}\\u003A){0,5}[0-9A-Fa-f]{1,4})?\\u003A{2}[0-9A-Fa-f]{1,4}|(?:(?:[0-9A-Fa-f]{1,4}\\u003A){0,6}[0-9A-Fa-f]{1,4})?\\u003A{2})|\\u0076[0-9A-Fa-f]+\\u002E(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A)+)\\u005D|(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])\\u002E(?:[0-9]|[\\u0031-\\u0039][0-9]|1[0-9]{2}|2[\\u0030-\\u0034][0-9]|25[\\u0030-\\u0035])|(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D])*)(?:\\u003A([0-9]*))?((?:\\u002F(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)*)*)|(\\u002F(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)+(?:\\u002F(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)*)*)|((?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)+(?:\\u002F(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)*)*)|(^\$))(?:\\u003F((?:(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)|\\u002F|\\u003F)*))?(?:\\u0023((?:(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|\\u0025[0-9A-Fa-f]{2}|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)|\\u002F|\\u003F)*))?"
private const val pchar =
    "(?:(?:[A-Za-z]|[0-9]|[\\u002D\\u002E\\u005F\\u007E])|(?:\\u0025[0-9A-Fa-f]{2})|[\\u0021\\u0024\\u0026\\u0027\\u0028\\u0029\\u002A\\u002B\\u002C\\u003B\\u003D]|\\u003A|\\u0040)"
private const val pseparator = "\\u002F"

private val regex = Regex(URI)
private const val SCHEME_INDEX = 1
private const val USERINFO_INDEX = 2
private const val HOST_INDEX = 3
private const val PORT_INDEX = 4
private const val PATH_INDEX_START = 5
private const val PATH_INDEX_END = 8
private const val QUERY_INDEX = 9
private const val FRAGMENT_INDEX = 10
private const val PORT_MAX_VALUE = 65535
internal const val FILE_SCHEME_NAME = "file"

fun UrlFactory.create(url: String): Url = regex.matchEntire(url)?.run {
    Url(
        scheme = groupValues[SCHEME_INDEX].lowercase(),
        authority = Authority(
            userInfo = groupValues[USERINFO_INDEX],
            host = groupValues[HOST_INDEX].ifEmpty {
                this@create.schemeInfo[groupValues[SCHEME_INDEX].lowercase()]?.defaultHost ?: ""
            },
            port = groupValues[PORT_INDEX].let {
                if (it.isEmpty()) {
                    this@create.schemeInfo[groupValues[SCHEME_INDEX].lowercase()]?.defaultPort ?: -1
                } else {
                    it.toInt().coerceIn(0..PORT_MAX_VALUE)
                }
            }
        ),
        path = Path((PATH_INDEX_START..PATH_INDEX_END).firstOrNull { groupValues[it].isNotBlank() }
            ?.let { groupValues[it] } ?: "/"),
        query = Query(groupValues[QUERY_INDEX]),
        fragment = groupValues[FRAGMENT_INDEX],
        source = this@create
    )
} ?: throw UrlParseException(url)

fun UrlFactory.createFactory(op: UrlFactoryBuilder.() -> Unit): UrlFactory = UrlFactoryBuilder(this).apply(op).build()

data class Query(val value: String) : Map<String, String> {
    private val queryMapping: Map<String, String> by lazy {
        if (value.isNotBlank()) {
            var queryKVIndex = 0
            val r = HashMap<String, String>()
            do {
                val end = value.indexOf('&', queryKVIndex)
                val separator = value.indexOf('=', queryKVIndex)
                r[value.substring(queryKVIndex until separator)] =
                    value.substring((separator + 1) until if (end < 0) value.length else end)
                queryKVIndex = end + 1
            } while (queryKVIndex > 0)
            r.toMap()
        } else {
            emptyMap()
        }
    }

    override val entries: Set<Map.Entry<String, String>>
        get() = queryMapping.entries
    override val keys: Set<String>
        get() = queryMapping.keys
    override val size: Int
        get() = queryMapping.size
    override val values: Collection<String>
        get() = queryMapping.values

    override fun containsKey(key: String): Boolean {
        return queryMapping.containsKey(key)
    }

    override fun containsValue(value: String): Boolean {
        return queryMapping.containsValue(value)
    }

    override fun get(key: String): String {
        return queryMapping[key] ?: ""
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun toString(): String {
        return value
    }

}

data class Authority(val userInfo: String, val host: String, val port: Int) {
    fun isEmpty() = userInfo.isEmpty() && host.isEmpty() && port < 0
    fun toString(showPort: Boolean): String {
        return "${if (userInfo.isEmpty()) "" else "$userInfo@"}$host${if (port < 0 || !showPort) "" else ":$port"}"
    }

    override fun toString(): String {
        return toString(true)
    }
}

data class Path(val value: String) : List<String> {
    private val parts: List<String> by lazy { value.split('/').dropWhile { it.isBlank() } }
    override fun iterator(): Iterator<String> = parts.iterator()
    override val size: Int
        get() = parts.size

    override fun contains(element: String): Boolean {
        return parts.contains(element)
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        return parts.containsAll(elements)
    }

    override fun get(index: Int): String {
        return parts[index]
    }

    override fun indexOf(element: String): Int {
        return parts.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty() || value == "/"
    }

    override fun lastIndexOf(element: String): Int {
        return parts.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<String> {
        return parts.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<String> {
        return parts.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<String> {
        return parts.subList(fromIndex, toIndex)
    }

    override fun toString(): String {
        return "/$value"
    }

}

expect fun String.percentEncoded(): String
expect fun String.percentDecoded(): String

class UrlParseException(val url: String) : Exception("Invalid url: $url")

data class UrlSchemeInfo(val name: String, val defaultPort: Int = -1, val defaultHost: String = "")