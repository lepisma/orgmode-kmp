package xyz.lepisma.orgmode.lexer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.datatest.withData

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class OrgDTParserTest : FunSpec ({
    val parser = OrgDTParser()

    val testData = listOf(
        Pair("<2023-10-27 Fri>", Token.DatetimeStamp(
            text = "<2023-10-27 Fri>",
            range = Pair(0, 16),
            date = LocalDate(2023, 10, 27), // Use LocalDate constructor
            showWeekDay = true,
            time = null,
            isActive = true,
            repeater = null
        )),
        Pair("[2024-01-15 Mon]", Token.DatetimeStamp(
            text = "[2024-01-15 Mon]",
            range = Pair(0, 16),
            date = LocalDate(2024, 1, 15), // Use LocalDate constructor
            showWeekDay = true,
            time = null,
            isActive = false,
            repeater = null
        )),
        Pair("<2023-11-01 Wed 09:00>", Token.DatetimeStamp(
            text = "<2023-11-01 Wed 09:00>",
            range = Pair(0, 22),
            date = LocalDate(2023, 11, 1),
            showWeekDay = true,
            time = Pair(LocalTime(9, 0), null), // Use LocalTime constructor
            isActive = true,
            repeater = null
        )),
        Pair("[2025-03-08 Sat 14:30]", Token.DatetimeStamp(
            text = "[2025-03-08 Sat 14:30]",
            range = Pair(0, 22),
            date = LocalDate(2025, 3, 8),
            showWeekDay = true,
            time = Pair(LocalTime(14, 30), null),
            isActive = false,
            repeater = null
        )),
        Pair("<2023-12-25 10:00-11:00>", Token.DatetimeStamp(
            text = "<2023-12-25 10:00-11:00>",
            range = Pair(0, 24),
            date = LocalDate(2023, 12, 25),
            showWeekDay = false,
            time = Pair(LocalTime(10, 0), LocalTime(11, 0)),
            isActive = true,
            repeater = null
        )),
        Pair("<2023-10-27 Fri +1d>", Token.DatetimeStamp(
            text = "<2023-10-27 Fri +1d>",
            range = Pair(0, 20),
            date = LocalDate(2023, 10, 27),
            showWeekDay = true,
            time = null,
            isActive = true,
            repeater = "+1d"
        )),
        Pair("<2023-10-27 Fri 10:30 .1w>", Token.DatetimeStamp(
            text = "<2023-10-27 Fri 10:30 .1w>",
            range = Pair(0, 26),
            date = LocalDate(2023, 10, 27),
            showWeekDay = true,
            time = Pair(LocalTime(10, 30), null),
            isActive = true,
            repeater = ".1w"
        )),
        Pair("<2023-10-27 10:20>", Token.DatetimeStamp(
            text = "<2023-10-27 10:20>",
            range = Pair(0, 18),
            date = LocalDate(2023, 10, 27),
            showWeekDay = false,
            time = Pair(LocalTime(10, 20), null),
            isActive = true,
            repeater = null
        )),
        Pair("<2023-10-26>", Token.DatetimeStamp(
            text = "<2023-10-26>",
            range = Pair(0, 12),
            date = LocalDate(2023, 10, 26),
            showWeekDay = false,
            time = null,
            isActive = true,
            repeater = null
        )),
        Pair("[2023-10-27 Fri 10:00]", Token.DatetimeStamp(
            text = "[2023-10-27 Fri 10:00]",
            range = Pair(0, 22),
            date = LocalDate(2023, 10, 27),
            showWeekDay = true,
            time = Pair(LocalTime(10, 0), null),
            isActive = false,
            repeater = null
        )),
        Pair("This is not a datetime", null),
        Pair("<2023-13-01 Mon>", null)
    )

    context("OrgDTParser should correctly parse datetime stamps") {
        withData(testData) { (inputString, expectedStamp) ->
            val actualToken = parser.parse(inputString, 0)
            actualToken shouldBe expectedStamp
        }
    }
})
