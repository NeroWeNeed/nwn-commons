package github.nwn.commons

import kotlinx.datetime.*


const val YEAR_TOKEN_NAME = "year"
const val MONTH_TOKEN_NAME = "month"
const val DAY_TOKEN_NAME = "day"
const val DAY_OF_YEAR_TOKEN_NAME = "day_of_year"
const val DAY_OF_WEEK_TOKEN_NAME = "weekday"
const val MINUTE_TOKEN_NAME = "minute"
const val SECOND_TOKEN_NAME = "second"
const val HOUR_TOKEN_NAME = "hour"
const val AM_PM_TOKEN_NAME = "am_pm"

const val TIMEZONE_OPTION_NAME = "timezone"
const val SHORT_OPTION_NAME = "short"
const val NAME_OPTION_NAME = "name"
const val MILITARY_TIME_OPTION_NAME = "military"
private val shortDaysOfWeek = mapOf(
    DayOfWeek.MONDAY to "mon",
    DayOfWeek.TUESDAY to "tues",
    DayOfWeek.WEDNESDAY to "wed",
    DayOfWeek.THURSDAY to "thurs",
    DayOfWeek.FRIDAY to "fri",
    DayOfWeek.SATURDAY to "sat",
    DayOfWeek.SUNDAY to "sun"
)

private fun getTokenName(prefix: String, base: String): String {
    return if (prefix.isNotBlank())
        "${prefix}_$base"
    else
        base
}

fun <T : Any> NamingTemplateCompilerBuilder<T>.currentDateTime() = dateTime("") { Clock.System.now() }
fun <T : Any> NamingTemplateCompilerBuilder<T>.dateTime(prefix: String, timeGetter: (T) -> Instant) {
    token<Int>(getTokenName(prefix, YEAR_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        operation { value, options ->
            timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).year
        }
    }
    token(getTokenName(prefix, MONTH_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        option(NAME_OPTION_NAME, true)
        textFormatOptions()
        operation { value, options, builder ->
            val short = options.getValue(SHORT_OPTION_NAME) as Boolean
            val name = options.getValue(NAME_OPTION_NAME) as Boolean
            val month = timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).month
            val str = format(
                when {
                    short && name -> {
                        month.name.substring(0 until 3).lowercase()
                    }
                    !short && name -> {
                        month.name.lowercase()
                    }
                    short && !name -> {
                        (month.ordinal + 1).toString()
                    }
                    else -> {
                        (month.ordinal + 1).toString().padStart(2, '0')
                    }
                }, options
            )
            builder.append(str)
        }
    }
    token(getTokenName(prefix, DAY_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        operation { value, options, builder ->
            val day = timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).dayOfMonth.let {
                if (options.getValue(SHORT_OPTION_NAME) as Boolean) {
                    it.toString()
                } else {
                    it.toString().padStart(2, '0')
                }
            }
            builder.append(day)
        }
    }
    token(getTokenName(prefix, DAY_OF_YEAR_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        operation { value, options, builder ->
            val day = timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).dayOfYear.let {
                if (options.getValue(SHORT_OPTION_NAME) as Boolean) {
                    it.toString()
                } else {
                    it.toString().padStart(3, '0')
                }
            }
            builder.append(day)
        }
    }
    token(getTokenName(prefix, DAY_OF_WEEK_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        option(NAME_OPTION_NAME, true)
        textFormatOptions()
        operation { value, options, builder ->
            val short = options.getValue(SHORT_OPTION_NAME) as Boolean
            val name = options.getValue(NAME_OPTION_NAME) as Boolean
            val dayOfWeek = timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).dayOfWeek
            val str = format(
                when {
                    short && name -> {
                        shortDaysOfWeek.getValue(dayOfWeek)
                    }
                    !short && name -> {
                        dayOfWeek.name.lowercase()
                    }
                    else -> {
                        dayOfWeek.isoDayNumber.toString()
                    }
                }, options
            )
            builder.append(str)
        }
    }
    token(getTokenName(prefix, SECOND_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        operation { value, options, builder ->
            val second = timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).second.let {
                if (options.getValue(SHORT_OPTION_NAME) as Boolean) {
                    it.toString()
                } else {
                    it.toString().padStart(2, '0')
                }
            }
            builder.append(second)
        }
    }
    token<String>(getTokenName(prefix, MINUTE_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        operation { value, options, builder ->
            val minute = timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).minute.let {
                if (options.getValue(SHORT_OPTION_NAME) as Boolean) {
                    it.toString()
                } else {
                    it.toString().padStart(2, '0')
                }
            }
            builder.append(minute)
        }
    }
    token<String>(getTokenName(prefix, HOUR_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        option(MILITARY_TIME_OPTION_NAME, true)
        operation { value, options ->
            timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).hour.let {
                val t = if (options.getValue(MILITARY_TIME_OPTION_NAME) as Boolean) {
                    it
                } else {
                    if (it % 12 == 0) 12 else (it % 12)
                }

                if (options.getValue(SHORT_OPTION_NAME) as Boolean) {
                    t.toString()
                } else {
                    t.toString().padStart(2, '0')
                }
            }

        }
    }
    token<String>(getTokenName(prefix, AM_PM_TOKEN_NAME)) {
        option(TIMEZONE_OPTION_NAME, TimeZone.UTC) {
            TimeZone.of(it)
        }
        option(SHORT_OPTION_NAME, false)
        textFormatOptions()
        operation { value, options ->
            val short = options.getValue(SHORT_OPTION_NAME) as Boolean
            timeGetter(value).toLocalDateTime(options.getValue("timezone") as TimeZone).hour.let {
                val isPm = it >= 12
                when {
                    short && isPm -> "p"
                    short && !isPm -> "a"
                    !short && isPm -> "pm"
                    else -> "am"
                }
            }

        }
    }
}