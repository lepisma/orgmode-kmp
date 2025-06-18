package xyz.lepisma.orgmode.lexer

import android.annotation.TargetApi
import android.os.Build
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

@TargetApi(Build.VERSION_CODES.O)
class OrgDTParser {
    private val DATE_FORMATTER_ORG = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val TIME_FORMATTER_ORG = DateTimeFormatter.ofPattern("HH:mm")

    private val REPEATER_PATTERN = """([.+]?\d+[hdwmy])""".toRegex() // e.g., +1d, .5m, ++1w

    private val SINGLE_TIMESTAMP_CONTENT_REGEX = """
    (\d{4}-\d{2}-\d{2})         # Group 1: YYYY-MM-DD
    (?:\s([A-Za-z]{3}))?        # Group 2: Optional Day (e.g., Mon, Tue)
    (?:\s(\d{2}:\d{2}))?        # Group 3: Optional Start Time HH:MM
    (?:-(\d{2}:\d{2}))?         # Group 4: Optional End Time HH:MM (if time range)
    (?:\s(${REPEATER_PATTERN.pattern}))?
    """.trimIndent().toRegex(RegexOption.COMMENTS)

    // These are exposed for lookahead
    val ACTIVE_TIMESTAMP_REGEX = """<($SINGLE_TIMESTAMP_CONTENT_REGEX)>""".toRegex(RegexOption.COMMENTS)
    val INACTIVE_TIMESTAMP_REGEX = """\[($SINGLE_TIMESTAMP_CONTENT_REGEX)\]""".toRegex(RegexOption.COMMENTS)

    fun parse(text: String, index: Int): Token.DatetimeStamp? {
        val isActive: Boolean
        val matchResult: MatchResult?

        when {
            ACTIVE_TIMESTAMP_REGEX.matchAt(text, index) != null -> {
                isActive = true
                matchResult = ACTIVE_TIMESTAMP_REGEX.find(text)
            }
            INACTIVE_TIMESTAMP_REGEX.matchAt(text, index) != null -> {
                isActive = false
                matchResult = INACTIVE_TIMESTAMP_REGEX.find(text)
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

                    val date = LocalDate.parse(dateStr, DATE_FORMATTER_ORG)

                    val showWeekDay = weekdayStr != null
                    if (showWeekDay) {
                        val expectedWeekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        if (!expectedWeekday.startsWith(weekdayStr!!, ignoreCase = true)) {
                            println("Warning: Weekday mismatch for '$dateStr'. Expected $expectedWeekday, got $weekdayStr")
                        }
                    }

                    val timePair: Pair<LocalTime, LocalTime?>? = if (timeStartStr != null) {
                        val startTime = LocalTime.parse(timeStartStr, TIME_FORMATTER_ORG)
                        val endTime = if (timeEndStr != null) LocalTime.parse(timeEndStr, TIME_FORMATTER_ORG) else null
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

                } catch (e: DateTimeParseException) {
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