package xyz.lepisma.orgmode.lexer

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.*


/**
 * Parse and return a DatetimeStamp Token.
 *
 * While this is designed to be used in the lexer, if needed, you can also use it in the parser.
 */
class OrgDTParser {
    private val DATE_FORMATTER_ORG = LocalDate.Format { year(); char('-'); monthNumber(); char('-'); dayOfMonth() }
    private val TIME_FORMATTER_ORG = LocalTime.Format { hour(); char(':'); minute() }

    private val REPEATER_PATTERN = """([.+]?\d+[hdwmy])""".toRegex() // e.g., +1d, .5m, ++1w
    private val SINGLE_TIMESTAMP_CONTENT_REGEX = """(\d{4}-\d{2}-\d{2})(?:\s([A-Za-z]{3}))?(?:\s(\d{2}:\d{2}))?(?:-(\d{2}:\d{2}))?(?:\s(${REPEATER_PATTERN.pattern}))?""".toRegex()
    // These are exposed for lookahead
    val ACTIVE_TIMESTAMP_REGEX = """<($SINGLE_TIMESTAMP_CONTENT_REGEX)>""".toRegex()
    val INACTIVE_TIMESTAMP_REGEX = """\[($SINGLE_TIMESTAMP_CONTENT_REGEX)\]""".toRegex()

    fun parse(text: String, index: Int): Token.DatetimeStamp? {
        val isActive: Boolean
        val matchResult: MatchResult?

        when {
            ACTIVE_TIMESTAMP_REGEX.matchAt(text, index) != null -> {
                isActive = true
                matchResult = ACTIVE_TIMESTAMP_REGEX.find(text, index)
            }
            INACTIVE_TIMESTAMP_REGEX.matchAt(text, index) != null -> {
                isActive = false
                matchResult = INACTIVE_TIMESTAMP_REGEX.find(text, index)
            }
            else -> return null
        }

        matchResult?.let { m ->
            val contentMatchResult = SINGLE_TIMESTAMP_CONTENT_REGEX.find(m.groupValues[1])
            contentMatchResult?.let { cm ->
                try {
                    val dateStr = cm.groupValues[1]
                    val weekdayStr = cm.groupValues[2].takeIf { it.isNotEmpty() }
                    val timeStartStr = cm.groupValues[3].takeIf { it.isNotEmpty() }
                    val timeEndStr = cm.groupValues[4].takeIf { it.isNotEmpty() }
                    val repeaterStr = cm.groupValues[5].takeIf { it.isNotEmpty() }

                    val date = try {
                        DATE_FORMATTER_ORG.parse(dateStr)
                    } catch (e: IllegalArgumentException) {
                        println("Error parsing date '$dateStr': ${e.message}")
                        return null
                    }

                    val showWeekDay = weekdayStr != null
                    if (showWeekDay) {
                        val expectedWeekday = date.dayOfWeek.name.take(3)
                        if (!expectedWeekday.equals(weekdayStr, ignoreCase = true)) {
                            println("Warning: Weekday mismatch for '$dateStr'. Expected $expectedWeekday, got $weekdayStr")
                        }
                    }

                    val timePair: Pair<LocalTime, LocalTime?>? = if (timeStartStr != null) {
                        val startTime = try {
                            TIME_FORMATTER_ORG.parse(timeStartStr)
                        } catch (e: IllegalArgumentException) {
                            println("Error parsing start time '$timeStartStr': ${e.message}")
                            return null
                        }

                        val endTime = if (timeEndStr != null) {
                            try {
                                TIME_FORMATTER_ORG.parse(timeEndStr)
                            } catch (e: IllegalArgumentException) {
                                println("Error parsing end time '$timeEndStr': ${e.message}")
                                return null
                            }
                        } else {
                            null
                        }
                        startTime to endTime
                    } else {
                        null
                    }

                    val repeater = repeaterStr.takeIf { it?.isNotEmpty() == true }
                    return Token.DatetimeStamp(
                        text = m.value,
                        range = Pair(index, index + m.value.length),
                        date = date,
                        showWeekDay = showWeekDay,
                        time = timePair,
                        isActive = isActive,
                        repeater = repeater
                    )

                } catch (e: IllegalArgumentException) {
                    println("Error parsing date/time for '${m.value}': ${e.message}")
                    return null
                } catch (e: Exception) {
                    println("Unexpected error parsing '${m.value}': ${e.message}")
                    return null
                }
            }
        }
        return null
    }
}