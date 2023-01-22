package github.nwn.commons

fun interface NamingOperationWithOptions<T : Any,U> {
    operator fun invoke(data: T, options: Map<String, Any>): U
}