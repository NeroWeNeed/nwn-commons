package github.nwn.commons

interface NamingTemplateCompilerBuilderScope<T : Any> {
    fun <U> token(name: String, op: NamingOperationBuilderScope<T,U>.() -> Unit)
}