package github.nwn.commons

import github.nwn.graph.graph
import kotlin.reflect.KClass

data class Token<T : Any, U>(
    val name: String,
    private val op: NamingOperationWithOptions<T, U>?,
    val options: Map<String, NamingOperationOption<*>>,
    private val formatter: NamingOperationResultFormatter<U>?
) {
    val isNoOp: Boolean
        get() = op == null

    fun createOperation(
        options: Map<String, Any> = emptyMap(),
        stringTransformers: List<(String) -> String> = emptyList()
    ) =
        TokenOperation(
            name,
            op,
            this.options.entries.associate { (k, v) -> k to (options[k] ?: v.defaultValue) },
            formatter,
            stringTransformers
        )

}

data class NamingOperationOption<T : Any>(val name: String, val defaultValue: T, val parser: (String) -> T) {
    fun parseValue(value: String): T = parser(value)
}

data class ConstantOperation<T : Any>(val text: String) : NamingOperation<T> {
    override fun invoke(data: T): String {
        return text
    }
}

data class TokenOperation<T : Any, U>(
    val name: String,
    private val op: NamingOperationWithOptions<T, U>?,
    val options: Map<String, Any>,
    private val formatter: NamingOperationResultFormatter<U>?,
    private val stringTransformers: List<(String) -> String>
) :
    NamingOperation<T> {
    override fun invoke(data: T): String {
        return op?.invoke(data, options)?.let { result ->
            (formatter?.format(result, options) ?: result.toString()).let { str ->
                stringTransformers.fold(str) { acc, op ->
                    op(acc)
                }
            }
        } ?: ""
    }

}

class NamingTemplate<T : Any>(
    private val operations: List<NamingOperation<T>>
) {
    fun format(value: T): String {
        val builder = StringBuilder()
        operations.forEach {
            builder.append(it.invoke(value))
        }
        return builder.toString()
    }

}

class NamingTemplateCompiler<T : Any>(
    private val tokens: Map<String, Token<T, *>>,
    private val formatters: Map<String, (String) -> String>
) {
    companion object {
        internal const val TOKEN_START = '<'
        internal const val TOKEN_END = '>'
        internal const val TOKEN_OPTION_DELIMITER = ';'
        internal const val TOKEN_OPTION_KEY_VALUE_SEPARATOR = '='
        internal const val ESCAPE = '\\'
    }

    private val templateCompiler = graph<NamingTemplateCompilerState<T>, String> {
        val textNode = nodeReference()
        val tokenNode = nodeReference()
        val optionKeyNode = nodeReference()
        val optionValueNode = nodeReference()
        val terminalNode = terminalNode()
        node(textNode) {
            step {
                if (state.index >= input.length)
                    return@step terminalNode
                val char = input[state.index++]
                when {
                    state.escaped -> {
                        state.buffer.append(char)
                        state.escaped = false
                        return@step textNode
                    }
                    char == ESCAPE -> {
                        state.escaped = true
                        return@step textNode
                    }
                    char == TOKEN_START -> {
                        return@step tokenNode
                    }
                    else -> {
                        state.buffer.append(char)
                        return@step textNode
                    }
                }
            }
            exit {
                if (state.buffer.isNotEmpty()) {
                    state.operations.add(ConstantOperation(state.buffer.toString()))
                    state.buffer.clear()
                }
            }
            enter {
                state.tokenName = ""
                state.tokenOptionName = ""
                state.tokenOptions.clear()
                state.tokenFormatters.clear()

            }
        }

        node(tokenNode) {
            step {
                if (state.index >= input.length) {
                    state.exception = NamingTemplateCompilerException("Malformed Token at ${state.index}")
                    return@step terminalNode
                }
                val char = input[state.index++]
                when {
                    state.escaped -> {
                        state.buffer.append(char)
                        state.escaped = false
                        return@step tokenNode
                    }
                    char == ESCAPE -> {
                        state.escaped = true
                        return@step tokenNode
                    }
                    char == TOKEN_END -> {
                        val token = tokens[state.buffer.toString().lowercase()]
                        if (token == null) {
                            state.exception = NamingTemplateCompilerException("Undefined Token at ${state.index}")
                            return@step terminalNode
                        } else {
                            state.operations.add(token.createOperation())
                            return@step textNode
                        }
                    }
                    char == TOKEN_OPTION_DELIMITER -> {
                        state.tokenName = state.buffer.toString().lowercase()
                        if (tokens.containsKey(state.tokenName)) {
                            return@step optionKeyNode
                        } else {
                            state.exception = NamingTemplateCompilerException("Undefined Token at ${state.index}")
                            return@step terminalNode
                        }
                    }
                    else -> {
                        state.buffer.append(char)
                        return@step tokenNode
                    }

                }

            }
            exit {
                state.buffer.clear()
            }
        }
        node(optionKeyNode) {
            step {
                if (state.index >= input.length) {
                    state.exception = NamingTemplateCompilerException("Malformed Token at ${state.index}")
                    return@step terminalNode
                }
                val char = input[state.index++]
                when {
                    state.escaped -> {
                        state.buffer.append(char)
                        state.escaped = false
                        return@step optionKeyNode
                    }
                    char == ESCAPE -> {
                        state.escaped = true
                        return@step optionKeyNode
                    }
                    char == TOKEN_END -> {
                        val optionName = state.buffer.toString().lowercase()
                        val token = tokens.getValue(state.tokenName)
                        val option = token.options[optionName]
                        if (option == null) {
                            val formatOption = formatters[optionName]
                            if (formatOption == null) {
                                state.exception =
                                    NamingTemplateCompilerException("Undefined Token option for token ${token.name} at ${state.index}")
                                return@step terminalNode
                            }
                            state.tokenFormatters.add(formatOption)
                        } else {
                            state.tokenOptions[option.name] = try {
                                option.parseValue("")
                            } catch (exception: Exception) {
                                state.exception =
                                    NamingTemplateCompilerException("Error parsing option value '' for option ${option.name} under ${token.name} at ${state.index}")
                                return@step terminalNode
                            }
                        }
                        state.operations.add(token.createOperation(state.tokenOptions, state.tokenFormatters))
                        state.buffer.clear()
                        return@step textNode


                    }
                    char == TOKEN_OPTION_DELIMITER -> {
                        val optionName = state.buffer.toString().lowercase()
                        val token = tokens.getValue(state.tokenName)
                        val option = token.options[optionName]
                        if (option == null) {
                            state.exception =
                                NamingTemplateCompilerException("Undefined Token option for token ${token.name} at ${state.index}")
                            return@step terminalNode
                        }
                        state.tokenOptions[option.name] = try {
                            option.parseValue("")
                        } catch (exception: Exception) {
                            state.exception =
                                NamingTemplateCompilerException("Error parsing option value '' for option ${option.name} under ${token.name} at ${state.index}")
                            return@step terminalNode
                        }
                        state.buffer.clear()
                        return@step optionKeyNode
                    }
                    char == TOKEN_OPTION_KEY_VALUE_SEPARATOR -> {
                        val optionName = state.buffer.toString().lowercase()
                        val token = tokens.getValue(state.tokenName)
                        val option = token.options[optionName]
                        if (option == null) {
                            state.exception =
                                NamingTemplateCompilerException("Undefined Token option for token ${token.name} at ${state.index}")
                            return@step terminalNode
                        }
                        state.tokenOptionName = optionName
                        state.buffer.clear()
                        return@step optionValueNode
                    }

                    else -> {
                        state.buffer.append(char)
                        return@step optionKeyNode
                    }

                }

            }
        }

        node(optionValueNode) {
            step {
                if (state.index >= input.length) {
                    state.exception = NamingTemplateCompilerException("Malformed Token at ${state.index}")
                    return@step terminalNode
                }
                val char = input[state.index++]
                when {
                    state.escaped -> {
                        state.buffer.append(char)
                        state.escaped = false
                        return@step optionValueNode
                    }
                    char == ESCAPE -> {
                        state.escaped = true
                        return@step optionValueNode
                    }
                    char == TOKEN_END -> {
                        val value = state.buffer.toString()
                        val token = tokens.getValue(state.tokenName)
                        val option = token.options.getValue(state.tokenOptionName)
                        state.tokenOptions[option.name] = try {
                            option.parseValue(value)
                        } catch (exception: Exception) {
                            state.exception =
                                NamingTemplateCompilerException("Error parsing option value '$value' for option ${option.name} under ${token.name} at ${state.index}")
                            return@step terminalNode
                        }
                        state.operations.add(token.createOperation(state.tokenOptions,state.tokenFormatters))
                        state.buffer.clear()
                        return@step textNode
                    }
                    char == TOKEN_OPTION_DELIMITER -> {
                        val value = state.buffer.toString()
                        val token = tokens.getValue(state.tokenName)
                        val option = token.options.getValue(state.tokenOptionName)
                        state.tokenOptions[option.name] = try {
                            option.parseValue(value)
                        } catch (exception: Exception) {
                            state.exception =
                                NamingTemplateCompilerException("Error parsing option value '$value' for option ${option.name} under ${token.name} at ${state.index}")
                            return@step terminalNode
                        }
                        state.buffer.clear()
                        return@step optionKeyNode
                    }
                    else -> {
                        state.buffer.append(char)
                        return@step optionValueNode
                    }
                }
            }
        }
    }

    fun compile(template: String) = NamingTemplateCompilerState<T>().run {
        templateCompiler.process(template, this)
        if (this.exception != null)
            throw exception!!
        NamingTemplate(this.operations.toList())
    }

    private class NamingTemplateCompilerState<T : Any>(
        val buffer: StringBuilder = StringBuilder(),
        val operations: MutableList<NamingOperation<T>> = ArrayList(),
        var index: Int = 0,
        var exception: NamingTemplateCompilerException? = null,
        var tokenName: String = "",
        var tokenOptionName: String = "",
        val tokenOptions: MutableMap<String, Any> = HashMap(),
        val tokenFormatters: MutableList<(String) -> String> = ArrayList(),
        var escaped: Boolean = false
    )
}

class NamingTemplateCompilerBuilder<T : Any> internal constructor() : NamingTemplateCompilerBuilderScope<T> {
    companion object {
        private val trueFalseMap = mapOf(
            "y" to true,
            "yes" to true,
            "t" to true,
            "true" to true,
            "on" to true,
            "1" to true,
            "n" to false,
            "no" to false,
            "f" to false,
            "false" to false,
            "off" to false,
            "0" to false
        )


    }


    private val tokens = HashMap<String, Token<T, *>>()
    private val formatters = HashMap<String, (String) -> String>()
    override fun <U> token(name: String, op: NamingOperationBuilderScope<T, U>.() -> Unit) {
        val token = NamingOperationBuilder<U>(name).apply(op).build()
        if (!token.isNoOp)
            tokens[name] = token
    }

    fun formatter(optionName: String, op: (String) -> String) {
        formatters[optionName] = op
    }

    internal fun build(): NamingTemplateCompiler<T> = NamingTemplateCompiler(tokens, formatters)


    inner class NamingOperationBuilder<U> internal constructor(private val name: String) :
        NamingOperationBuilderScope<T, U> {


        private var operation: NamingOperationWithOptions<T, U>? = null
        private val options = ArrayList<NamingOperationOption<*>>()
        private var formatter: NamingOperationResultFormatter<U>? = null
        override fun <V : Any> option(name: String, defaultValue: V, type: KClass<V>) {
            val option = when (type) {
                Byte::class -> NamingOperationOption(name, defaultValue) { it.toByte() }
                Short::class -> NamingOperationOption(name, defaultValue) { it.toShort() }
                Int::class -> NamingOperationOption(name, defaultValue) { it.toInt() }
                Long::class -> NamingOperationOption(name, defaultValue) { it.toLong() }
                UByte::class -> NamingOperationOption(name, defaultValue) { it.toUByte() }
                UShort::class -> NamingOperationOption(name, defaultValue) { it.toUShort() }
                UInt::class -> NamingOperationOption(name, defaultValue) { it.toUInt() }
                ULong::class -> NamingOperationOption(name, defaultValue) { it.toULong() }
                Float::class -> NamingOperationOption(name, defaultValue) { it.toFloat() }
                Boolean::class -> NamingOperationOption(name, defaultValue) { trueFalseMap[it.lowercase()] ?: true }
                Double::class -> NamingOperationOption(name, defaultValue) { it.toDouble() }
                String::class -> NamingOperationOption(name, defaultValue) { it }
                else -> throw IllegalArgumentException("Unsupported option type. Please provide parser.")
            }
            options.add(option)
        }

        override fun <V : Any> option(name: String, defaultValue: V, parser: (String) -> V, type: KClass<V>) {
            options.add(NamingOperationOption(name, defaultValue, parser))
        }

        override fun operation(op: NamingOperationWithOptions<T, U>) {
            operation = op
        }

        override fun formatter(op: NamingOperationResultFormatter<U>) {
            formatter = op
        }

        internal fun build() = Token(name, operation, options.associateBy { it.name.lowercase() }, formatter)
    }
}

fun <T : Any> NamingTemplateCompiler(op: NamingTemplateCompilerBuilder<T>.() -> Unit): NamingTemplateCompiler<T> =
    NamingTemplateCompilerBuilder<T>().apply(op).build()

open class NamingTemplateCompilerException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}

class NamingTemplateFormatException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}