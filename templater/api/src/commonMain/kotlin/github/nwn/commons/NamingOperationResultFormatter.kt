package github.nwn.commons

fun interface NamingOperationResultFormatter<T > {
    fun format(value: T, options: Map<String, Any?>): String
}