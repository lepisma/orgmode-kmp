package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import xyz.lepisma.orgmode.core.Parser

/**
 * A piece of text in org mode with consistent styling and interpretation
 */
sealed class OrgInlineElem {
    data class Text(
        val text: String,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class DTStamp(
        val date: LocalDate,
        val showWeekDay: Boolean,
        val time: Pair<LocalTime, LocalTime?>?,
        val isActive: Boolean,
        val repeater: String?,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem {
        companion object {
            fun fromDatetimeToken(stamp: Token.DatetimeStamp): DTStamp {
                return DTStamp(
                    date = stamp.date,
                    showWeekDay = stamp.showWeekDay,
                    time = stamp.time,
                    isActive = stamp.isActive,
                    repeater = stamp.repeater,
                    tokens = listOf(stamp)
                )
            }
        }
    }

    data class DTRange(
        val start: DTStamp,
        val end: DTStamp,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Link(
        val type: String?,
        val target: String,
        val title: OrgLine?,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Citation(
        val citeString: String,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Footnote(
        val key: String?,
        val text: OrgLine,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class InlineMath(
        val style: InlineMathStyle,
        val text: String,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem
    enum class InlineMathStyle { AMS, DOLLAR }

    data class InlineQuote(
        val type: InlineQuoteType,
        val text: String,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem
    enum class InlineQuoteType { HTML, LATEX }

    data class Bold(
        val content: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Italic(
        val content: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Underline(
        val content: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class StrikeThrough(
        val content: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Verbatim(
        val content: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    data class Code(
        val content: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem
}

/**
 * Take given tokens and parse as well formatted org inline elements
 */
fun buildInlineElems(tokens: List<Token>): List<OrgInlineElem> {
    return tokens.map { tok -> OrgInlineElem.Text(tok.text, tokens=listOf(tok)) }
}