package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token
import kotlinx.datetime.LocalDate
import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchLineBreak
import xyz.lepisma.orgmode.core.matchSpaces
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.maybe
import xyz.lepisma.orgmode.core.oneOrMore
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.zeroOrMore

/**
 * Preamble is everything that comes in the start before the actual content starts
 */
data class OrgPreamble(
    val title: OrgLine,
    val author: String? = null,
    val email: String? = null,
    val date: LocalDate? = null,
    val category: String? = null,
    val filetags: OrgTags? = null,
    val tags: OrgTags? = null,  // NOTE: I use this wrongly in pile
    val options: OrgOptions? = null,
    val pile: OrgOptions? = null,
    val properties: OrgProperties? = null,
    override var tokens: List<Token>
) : OrgElem


val parseFileKeyword: Parser<Pair<OrgToken, OrgLine>> = seq(
    (::matchToken) { it is Token.FileKeyword },
    matchSpaces,
    parseOrgLine
).map { (k, sp, line) ->
    line.tokens = collectTokens(sp, line)
    Pair(k, line)
}

/**
 * Preamble is everything before the start of actual content.
 *
 * We don't allow empty lines here
 */
val parsePreamble: Parser<OrgPreamble> = seq(
    maybe(seq(parseProperties, oneOrMore(matchLineBreak))),
    oneOrMore(seq(parseFileKeyword, matchLineBreak)),
    zeroOrMore(matchLineBreak)
).map { (propBlock, keywordLines, lbsEnd) ->
    // We need to interpret all the file keywords
    var title: OrgLine? = null
    var tags: OrgTags? = null

    for ((kwMatch, _) in keywordLines) {
        val token = kwMatch.first.tokens[0] as Token.FileKeyword
        val valueLine = kwMatch.second

        when (token.type) {
            Token.FileKeywordType.TITLE -> title = valueLine
            Token.FileKeywordType.TAGS -> tags = orgLineToTags(valueLine)
            else -> { }
        }
    }

    if (title == null) {
        println("Unable to parse Title")
    }

    OrgPreamble(
        title = title ?: OrgLine(emptyList(), tokens = emptyList()),
        tags = tags,
        tokens = collectTokens(propBlock, keywordLines, lbsEnd),
        properties = propBlock?.first
    )
}

fun orgLineToTags(line: OrgLine): OrgTags {
    val rawText = line.items
        .filter { it is OrgInlineElem.Text }
        .joinToString("") { (it as OrgInlineElem.Text).text }

    return OrgTags(
        tags = rawText.split(",").map { it.trim() }.filter { it.isNotBlank() },
        tokens = line.tokens
    )
}
