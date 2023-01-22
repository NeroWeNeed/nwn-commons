package github.nwn.commons

enum class TextFormatOption(private val op: (String) -> String) {
    NONE({ it }), UPPERCASE({ it.uppercase() }), LOWERCASE({ it.lowercase() }), CAPITALIZE({ a ->
        a.lowercase().replaceFirstChar { it.titlecaseChar() }
    });

    companion object {
        private val parseMap = mapOf<String, TextFormatOption>(
            "upper" to UPPERCASE,
            "uppercase" to UPPERCASE,
            "u" to UPPERCASE,
            "lower" to LOWERCASE,
            "lowercase" to LOWERCASE,
            "l" to LOWERCASE,
            "capitalize" to CAPITALIZE,
            "titlecase" to CAPITALIZE,
            "title" to CAPITALIZE,
            "t" to CAPITALIZE,
            "c" to CAPITALIZE,
            "none" to NONE,
            "no" to NONE,
            "n" to NONE,
            "0" to NONE,
        )

        fun parseFormatOption(str: String) = parseMap[str.lowercase()] ?: NONE
    }

    operator fun invoke(str: String) = op(str)
}

const val FORMAT_OPTION = "format"
const val TRIM_OPTION = "trim"
fun <T : Any> NamingTemplateCompilerBuilder<T>.textFormatOptions() {
    formatter("uppercase") { it.uppercase() }
    formatter("lowercase") { it.lowercase() }
    formatter("capitalize") { a -> a.replaceFirstChar { b -> if (b.isLowerCase()) b.titlecase() else b.toString() } }
    formatter("trim") { it.trim() }
    formatter("trim_start") { it.trimStart() }
    formatter("trim_end") { it.trimEnd() }
}
