package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.collectTokens

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
        val title: List<OrgInlineElem>?,
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
 *
 * This will not be very robust to begin with but will work for most use cases.
 */
fun buildInlineElems(tokens: List<Token>): List<OrgInlineElem> {
    // TODO for today, then release:
    // #tag
    // #metricValue(<num>)
    var elems: MutableList<OrgInlineElem> = mutableListOf()
    var currentPos = 0
    var linkTokens: MutableList<Token> = mutableListOf()

    while (currentPos < tokens.size) {
        val token = tokens[currentPos]

        when (token) {
            is Token.DatetimeStamp -> {
                elems.add(OrgInlineElem.DTStamp.fromDatetimeToken(token))
            }
            is Token.DatetimeDateRangeSep -> {
                // First we check if the last was dt stamp and next was dt stamp, then we add the
                // range. Otherwise we just add this as plain text elem.
                if (elems.isNotEmpty() &&
                    elems.last() is OrgInlineElem.DTStamp &&
                    currentPos + 1 < tokens.size &&
                    tokens[currentPos + 1] is Token.DatetimeStamp) {

                    val startStamp = elems.removeAt(elems.lastIndex) as OrgInlineElem.DTStamp
                    val endStamp = OrgInlineElem.DTStamp.fromDatetimeToken(tokens[currentPos + 1] as Token.DatetimeStamp)

                    elems.add(OrgInlineElem.DTRange(
                        start = startStamp,
                        end = endStamp,
                        tokens = startStamp.tokens + listOf(token) + endStamp.tokens
                    ))

                    // We also need to advance the position by one extra token
                    currentPos++
                } else {
                    elems.add(OrgInlineElem.Text(token.text, tokens=listOf(token)))
                }
            }
            is Token.LinkStart -> {
                // We assume no link-nesting
                linkTokens = mutableListOf(token)
            }
            is Token.LinkEnd -> {
                linkTokens.add(token)
                // Build the element now and append to list
                val sepIndex = linkTokens.indexOfFirst { it is Token.LinkTitleSep }
                var title: List<OrgInlineElem>? = null
                var type: String? = null

                val targetString = if (sepIndex == -1) {
                    // Everything inside the bounds is target
                    linkTokens.drop(1).dropLast(1).joinToString("") { it.text }
                } else {
                    // Everything till sep is target, rest is title
                    val titleTokens = linkTokens.subList(sepIndex + 1, linkTokens.size)
                    title = buildInlineElems(titleTokens)

                    linkTokens.subList(1, sepIndex).joinToString("") { it.text }
                }

                val splits = targetString.split(":", limit = 2)
                val target = when (splits.size) {
                    1 -> targetString
                    2 -> {
                        type = splits[0]
                        splits[1]
                    }
                    else -> "NO TARGET"
                }

                elems.add(OrgInlineElem.Link(
                    title = title,
                    target = target,
                    type = type,
                    tokens = linkTokens
                ))
            }
            is Token.LinkTitleSep -> {
                linkTokens.add(token)
            }
            else -> {
                if (linkTokens.isNotEmpty()) {
                    linkTokens.add(token)
                } else {
                    elems.add(OrgInlineElem.Text(token.text, tokens = listOf(token)))
                }
            }
        }
        currentPos++
    }

    return elems
}