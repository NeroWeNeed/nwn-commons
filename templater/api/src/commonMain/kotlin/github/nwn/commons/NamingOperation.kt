package github.nwn.commons

fun interface NamingOperation<T : Any> {
    operator fun invoke(data: T) : String
}