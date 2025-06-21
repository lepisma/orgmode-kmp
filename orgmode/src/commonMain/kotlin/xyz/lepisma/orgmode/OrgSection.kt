package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchLineBreak
import xyz.lepisma.orgmode.core.matchSpace
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.oneOrMore
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.zeroOrMore
import xyz.lepisma.orgmode.lexer.Token

/**
 * A section is an org chunk with heading attached to it
 */
data class OrgSection(
    val heading: OrgHeading,
    val body: List<OrgChunk>,
    override var tokens: List<Token>
) : OrgChunk(), OrgElem

data class OrgHeading(
    val title: OrgLine,
    val level: OrgHeadingLevel,
    val tags: OrgTags? = null,
    val todoState: OrgTODOState? = null,
    val priority: OrgPriority? = null,
    val planningInfo: OrgPlanningInfo? = null,
    val properties: OrgProperties? = null,
    override var tokens: List<Token>
) : OrgElem

data class OrgHeadingLevel(
    val level: Int,
    override var tokens: List<Token>
) : OrgElem

data class OrgPlanningInfo(
    val scheduled: OrgInlineElem.DTStamp?,
    val deadline: OrgInlineElem.DTStamp?,
    val closed: OrgInlineElem.DTStamp?,
    override var tokens: List<Token>
) : OrgElem

data class OrgPriority(
    val priority: Int,
    val text: String,
    override var tokens: List<Token>
) : OrgElem

data class OrgTODOState(
    val text: String,
    val isDone: Boolean,
    override var tokens: List<Token>
) : OrgElem

val parseLevel: Parser<OrgHeadingLevel> = matchToken {
    it is Token.HeadingStars
}.map { output ->
    OrgHeadingLevel(
        level = (output.tokens.first() as Token.HeadingStars).level,
        tokens = output.tokens
    )
}

// TODO: Fix this to handle tags
val parseHeadingTitle: Parser<OrgLine> = parseOrgLine

val parseHeading: Parser<OrgHeading> = seq(
    parseLevel,
    matchSpace,
    // maybe(::parseTODOState),
    // maybe(::parsePriority),
    parseHeadingTitle,
    oneOrMore(matchLineBreak),
    // maybe(::parseHeadingTags),
    // ::parsePlanningInfo,
    // maybe(::parseProperties)
).map { (level, sp, title, lbs) ->
    OrgHeading(
        level = level,
        title = title,
        tokens = collectTokens(level, sp, title, lbs)
    )
}

val parseSection: Parser<OrgSection> = seq(
    parseHeading,
    zeroOrMore(parseChunk)
).map { (heading, chunks) ->
    OrgSection(
        heading = heading,
        body = chunks,
        tokens = collectTokens(heading, chunks)
    )
}
