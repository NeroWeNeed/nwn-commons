import kotlinx.serialization.Serializable

@Serializable
data class UrlSchemeInfo(val name: String, val port: Int = -1, val host: String = "")


