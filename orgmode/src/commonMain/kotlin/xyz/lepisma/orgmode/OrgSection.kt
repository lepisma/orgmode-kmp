package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.collectUntil
import xyz.lepisma.orgmode.core.debug
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchLineBreak
import xyz.lepisma.orgmode.core.matchSpace
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.matchWord
import xyz.lepisma.orgmode.core.maybe
import xyz.lepisma.orgmode.core.oneOf
import xyz.lepisma.orgmode.core.oneOrMore
import xyz.lepisma.orgmode.core.parsingError
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

val parseHeadingTags: Parser<OrgTags> = matchToken { it is Token.TagString }
    .map { tok ->
        OrgTags(tags = (tok.tokens.first() as Token.TagString).tags, tokens = tok.tokens)
    }

val parseHeadingTitle: Parser<OrgLine> = collectUntil {
    it is Token.EOF || it is Token.LineBreak || it is Token.TagString
}.map { tokens ->
    OrgLine(
        items = tokens.map { tok ->
            OrgInlineElem.Text(
                tok.text,
                tokens = listOf(tok)
            )
        },
        tokens = tokens
    )
}

private fun parsePartialPlanningInfo(typeMatchFn: (Token) -> Boolean): Parser<OrgPlanningInfo> {
    return seq(
        matchToken { typeMatchFn(it) },
        matchSpace,
        matchToken { it is Token.DatetimeStamp },
        maybe(seq(
            matchToken { it is Token.DatetimeDateRangeSep },
            matchToken { it is Token.DatetimeStamp }
        ))
    ).map { (typeTok, sp, startStamp) ->
        var scheduled: OrgInlineElem.DTStamp? = null
        var closed: OrgInlineElem.DTStamp? = null
        var deadline: OrgInlineElem.DTStamp? = null
        val start: OrgInlineElem.DTStamp = OrgInlineElem.DTStamp.fromDatetimeToken(
            startStamp.tokens.first() as Token.DatetimeStamp
        )

        // TODO: We are not using the endStamp for now.
        when (typeTok.tokens.first()) {
            is Token.Closed -> closed = start
            is Token.Scheduled -> scheduled = start
            is Token.Deadline -> deadline = start
            else -> { }
        }

        OrgPlanningInfo(
            scheduled = scheduled,
            closed = closed,
            deadline = deadline,
            tokens = collectTokens(typeTok, sp, startStamp)
        )
    }
}

val parsePlanningInfo: Parser<OrgPlanningInfo> = oneOrMore(seq(
    oneOf(
        parsePartialPlanningInfo { it is Token.Scheduled },
        parsePartialPlanningInfo { it is Token.Closed },
        parsePartialPlanningInfo { it is Token.Deadline }
    ),
    maybe(matchSpace)
)).map { partials ->
    val scheduled = partials.firstNotNullOfOrNull { it.first.scheduled }
    val deadline = partials.firstNotNullOfOrNull { it.first.deadline }
    val closed = partials.firstNotNullOfOrNull { it.first.closed }

    OrgPlanningInfo(
        scheduled = scheduled,
        deadline = deadline,
        closed = closed,
        tokens = collectTokens(partials)
    )
}

val parseHeading: Parser<OrgHeading> = seq(
    parseLevel,
    matchSpace,
    // maybe(::parseTODOState),
    // maybe(::parsePriority),
    parseHeadingTitle, maybe(parseHeadingTags), oneOrMore(matchLineBreak),
    maybe(seq(parsePlanningInfo, oneOrMore(matchLineBreak))),
    // TODO: This, and many other blocks, are going to fail at the end of file
    maybe(seq(parseProperties, oneOrMore(matchLineBreak))),
).map { (level, sp, title, tags, lbs, planningBlock, propsBlock) ->
    OrgHeading(
        level = level,
        title = title,
        tags = tags,
        properties = propsBlock?.first,
        planningInfo = planningBlock?.first,
        tokens = collectTokens(level, sp, title, tags, lbs, planningBlock, propsBlock)
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
