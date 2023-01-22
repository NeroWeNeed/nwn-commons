package github.nwn.commons

import kotlin.reflect.KClass

interface NamingOperationBuilderScope<T : Any, U> {
    fun <V : Any> option(name: String, defaultValue: V, type: KClass<V>)
    fun <V : Any> option(name: String, defaultValue: V, parser: (String) -> V, type: KClass<V>)
    fun operation(op: NamingOperationWithOptions<T, U>)
    fun formatter(op: NamingOperationResultFormatter<U>)
}

inline fun <reified V : Any, T : Any, U> NamingOperationBuilderScope<T, U>.option(name: String, defaultValue: V) =
    option(name, defaultValue, V::class)

inline fun <reified V : Any, T : Any, U> NamingOperationBuilderScope<T, U>.option(
    name: String,
    defaultValue: V,
    noinline parser: (String) -> V
) =
    option(name, defaultValue, parser, V::class)