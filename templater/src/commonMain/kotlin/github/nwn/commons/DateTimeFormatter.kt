package github.nwn.commons

import github.nwn.graph.graph
import kotlinx.datetime.LocalDateTime

object DateTimeFormatter : NamingOperationResultFormatter<LocalDateTime> {
    private val tokens = DateTimeFormatters {
        token('y', 2, 4) { datetime, width, builder ->
            when (width) {
                2 -> builder.append((datetime.year % 100).toString().padStart(2,'0'))
                3 -> {
                    builder.append((datetime.year % 100).toString().padStart(3,'0'))
                }
                4 -> builder.append(datetime.year.toString().padStart(width,'0'))
            }
        }
        token('D', 1, 3) { datetime, width, builder ->
            builder.append(datetime.dayOfYear.toString().padStart(width,'0'))
        }
        token('M', 1, 4) { datetime, width, builder ->

            when (width) {
                1 -> builder.append(datetime.monthNumber)
                2 -> builder.append(datetime.monthNumber.toString().padStart(2,'0'))
                3 -> builder.append(datetime.month.)
            }
        }
    }
    private val parser = graph<ParserState, String> {
        val terminalNode = terminalNode()
        node {
            step {
                if (state.index >= input.length) {
                    return@step terminalNode
                }
                val char = input[state.index++]
                when {
                    char == '\'' -> {
                        if (state.escaped) {

                        } else {
                            state.escaped = true
                        }
                    }
                    state.escaped -> {

                    }

                }

                TODO()
            }
        }
    }

    override fun format(value: LocalDateTime, options: Map<String, Any?>): String {

        TODO()
    }

    data class ParserState(
        val dateTime: LocalDateTime,
        val buffer: StringBuilder = StringBuilder(),
        val output: StringBuilder = StringBuilder(),
        var escaped: Boolean = false,
        var index: Int = 0
    )

    data class Token(val minWidth: Int, val maxWidth: Int, val op: (LocalDateTime, Int, Int, StringBuilder) -> Unit)

}

fun interface DateTimeFormatterToken {
    fun format(dateTime: LocalDateTime, width: Int, buffer: StringBuilder)
    data class Impl(val minWidth: Int, val maxWidth: Int, private val formatter: DateTimeFormatterToken) :
        DateTimeFormatterToken {
        override fun format(dateTime: LocalDateTime, width: Int, buffer: StringBuilder) {
            formatter.format(dateTime, width.coerceIn(minWidth, maxWidth), buffer)
        }
    }
}

fun DateTimeFormatters(op: DateTimeFormatterTokenSetBuilder.() -> Unit) =
    DateTimeFormatterTokenSetBuilder().apply(op).build()

class DateTimeFormatterTokenSetBuilder internal constructor() {
    private val result = HashMap<Char, DateTimeFormatterToken>()
    fun token(code: Char, minWidth: Int = 1, maxWidth: Int = 10, op: DateTimeFormatterToken) {
        result[code] = DateTimeFormatterToken.Impl(minWidth, maxWidth, op)
    }

    internal fun build() = result.toMap()
}