package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.ParsingResult
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.collectUntil
import xyz.lepisma.orgmode.core.debug
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.matchWord
import xyz.lepisma.orgmode.core.maybe
import xyz.lepisma.orgmode.core.oneOf
import xyz.lepisma.orgmode.core.oneOrMore
import xyz.lepisma.orgmode.core.parsingError
import xyz.lepisma.orgmode.core.seq
import kotlin.math.min

/**
 * A piece of text in org mode with consistent styling and interpretation
 */
sealed class OrgInlineElem: OrgElem {
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

    // Items like #hash-tag. Not part of org-mode spec.
    data class HashTag(
        val text: String,  // Text without '#'
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

    // Items like #metric(value). These are not part of org-mode spec.
    data class HashMetric(
        val metric: String,  // Metric name without '#'
        val value: String,   // Value inside parentheses
        override var tokens: List<Token>
    ) : OrgInlineElem(), OrgElem

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
 * Parse hashtags like #hash #hash-tag etc.
 */
val parseHashTag: Parser<OrgInlineElem.HashTag> = seq(
    matchToken { it is Token.Text && it.text == "#" },
    collectUntil { it !is Token.Text || Regex("[a-zA-Z0-9_-]+").matchEntire(it.text) == null }
).map { (hashTok, tokens) ->
    OrgInlineElem.HashTag(
        text = tokens.joinToString("") { it.text },
        tokens = collectTokens(hashTok, tokens)
    )
}

/**
 * Parse tags with metrics like #hash-tag(value)
 */
val parseHashMetric: Parser<OrgInlineElem.HashMetric> = seq(
    matchToken { it is Token.Text && it.text == "#" },
    collectUntil { it !is Token.Text || Regex("[a-zA-Z0-9_-]+").matchEntire(it.text) == null },
    matchToken { it is Token.Text && it.text == "(" },
    // This might need more reinforcements
    collectUntil { it !is Token.Text || it.text == ")" },
    matchToken { it is Token.Text && it.text == ")" }
).map { (hashTok, metricTokens, lparen, valueTokens, rparen) ->
    OrgInlineElem.HashMetric(
        metric = metricTokens.joinToString("") { it.text },
        value = valueTokens.joinToString("") { it.text },
        tokens = collectTokens(hashTok, metricTokens, lparen, valueTokens, rparen)
    )
}

/**
 * Parse an org mode link
 */
val parseLink: Parser<OrgInlineElem.Link> = Parser { tokens, pos ->
    val startTokenResult = matchToken { it is Token.LinkStart }.invoke(tokens, pos)
    if (startTokenResult is ParsingResult.Failure) {
        return@Parser startTokenResult as ParsingResult.Failure<OrgInlineElem.Link>
    }

    // Try parsing the [[target][description]] form first
    val descParser = seq(
        parseInlineElems {
            it.firstOrNull() is Token.LinkTitleSep || it.firstOrNull() is Token.LinkEnd
        },
        matchToken { it is Token.LinkTitleSep },
        parseInlineElems { it.firstOrNull() is Token.LinkEnd },
        matchToken { it is Token.LinkEnd }
    )

    when (val descResult = descParser.invoke(tokens, (startTokenResult as ParsingResult.Success).nextPos)) {
        is ParsingResult.Success -> {
            val (targetElems, _, descElems, endToken) = descResult.output
            val targetString = targetElems.joinToString("") { (it as OrgInlineElem.Text).text }
            val (type, target) = targetString.split(":", limit = 2).let {
                if (it.size == 2) it[0] to it[1] else null to it[0]
            }
            return@Parser ParsingResult.Success(
                OrgInlineElem.Link(type, target, descElems, collectTokens(startTokenResult.output, descResult.output)),
                descResult.nextPos
            )
        }
        is ParsingResult.Failure -> {
            // Fallback to parsing the [[target]] form
            val targetParser = seq(
                parseInlineElems { it.firstOrNull() is Token.LinkEnd },
                matchToken { it is Token.LinkEnd }
            )
            when (val targetResult = targetParser.invoke(tokens, startTokenResult.nextPos)) {
                is ParsingResult.Success -> {
                    val (targetElems, endToken) = targetResult.output
                    val targetString = targetElems.joinToString("") { (it as OrgInlineElem.Text).text }
                    val (type, target) = targetString.split(":", limit = 2).let {
                        if (it.size == 2) it[0] to it[1] else null to it[0]
                    }
                    return@Parser ParsingResult.Success(
                        OrgInlineElem.Link(type, target, null, collectTokens(startTokenResult.output, targetResult.output)),
                        targetResult.nextPos
                    )
                }
                is ParsingResult.Failure -> return@Parser parsingError("Unclosed link")
            }
        }
    }
}

private val parsePlainText: Parser<OrgInlineElem.Text> = matchToken { token ->
    // Only match text tokens that are not parsed (yet) by special parsers
    // Also a lot of stopping responsibility is on the broader stopping function
    token !is Token.LinkStart &&
            token !is Token.LinkTitleSep &&
            token !is Token.LinkEnd &&
            token !is Token.DatetimeStamp
}.map { OrgInlineElem.Text(it.tokens.first().text, tokens = it.tokens) }

/**
 * Parse single datetime stamp
 */
val parseDTStamp: Parser<OrgInlineElem.DTStamp> = matchToken { it is Token.DatetimeStamp }.map { oTok ->
    val tok = oTok.tokens.first() as Token.DatetimeStamp
    OrgInlineElem.DTStamp.fromDatetimeToken(tok)
}

/**
 * Parse a datetime range. This is not just time range which is usually represented in the datetime
 * stamp itself.
 */
val parseDTRange: Parser<OrgInlineElem.DTRange> = seq(
    parseDTStamp,
    matchToken { it is Token.DatetimeDateRangeSep },
    parseDTStamp
).map { (startStamp, sep, endStamp) ->
    OrgInlineElem.DTRange(
        start = startStamp,
        end = endStamp,
        tokens = collectTokens(startStamp, sep, endStamp)
    )
}

private val parseSingleInlineElem: Parser<OrgInlineElem> = oneOf(
    // Order is important
    parseDTRange,
    parseDTStamp,
    parseLink,
    parseHashMetric,
    parseHashTag,
    //parseBold,
    //parseItalic,
    //parseUnderline,
    //parseStrikeThrough,
    //parseVerbatim,
    //parseCode,
    parsePlainText // Fallback for any non-special text
)

/**
 * Build a parser that continues to parse inline element till stopping criteria is met.
 *
 * @param stoppingFn takes a list of token trail and returns whether the processing should
 *        stop. In addition to this, processing also stops when tokens are exhausted.
 */
fun parseInlineElems(stoppingFn: (List<Token>) -> Boolean = { false }): Parser<List<OrgInlineElem>> {
    return Parser { tokens, pos ->
        val startingPos = pos
        var currentPos = startingPos
        val elements = mutableListOf<OrgInlineElem>()

        while (currentPos < tokens.size) {
            // This is the tokens that have been under consideration for the parse. The trail can be
            // used by the stopping function to decide whether to stop or not.
            val trail = tokens.subList(startingPos, min(currentPos + 1, tokens.size))
            if (stoppingFn(trail)) {
                break
            }

            when (val result = parseSingleInlineElem.invoke(tokens, currentPos)) {
                is ParsingResult.Success -> {
                    elements.add(result.output)
                    currentPos = result.nextPos
                }
                is ParsingResult.Failure -> {
                    // This means no parser could handle the current token.
                    // This could be a syntax error or the end of a valid sequence.
                    // For now, we stop successfully with what we have.
                    break
                }
            }
        }
        if (elements.isEmpty()) {
            return@Parser parsingError("No inline elements parsed")
        }
        ParsingResult.Success(elements, currentPos)
    }
}

private fun formatInlineElemToPlainText(elem: OrgInlineElem): String {
    return when (elem) {
        is OrgInlineElem.Link -> {
            if (elem.title != null) {
                formatInlineElemsToPlaintext(elem.title)
            } else if (elem.type != null) {
                "${elem.type}:${elem.target}"
            } else {
                elem.target
            }
        }
        is OrgInlineElem.Bold -> formatInlineElemsToPlaintext(elem.content)
        is OrgInlineElem.Italic -> formatInlineElemsToPlaintext(elem.content)
        is OrgInlineElem.Underline -> formatInlineElemsToPlaintext(elem.content)
        is OrgInlineElem.StrikeThrough-> formatInlineElemsToPlaintext(elem.content)
        is OrgInlineElem.Verbatim -> formatInlineElemsToPlaintext(elem.content)
        is OrgInlineElem.Code -> formatInlineElemsToPlaintext(elem.content)
        else ->  elem.tokens.joinToString("") { it.text }
    }
}

/**
 * Convert list of inline elements to plain text. This is not the same as recovering the original
 * org mode string since markups, links etc. are ignored here.
 */
fun formatInlineElemsToPlaintext(elems: List<OrgInlineElem>): String {
    return elems.joinToString("") { formatInlineElemToPlainText(it) }
}