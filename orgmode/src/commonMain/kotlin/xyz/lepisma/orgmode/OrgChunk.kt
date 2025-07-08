package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.ParsingResult
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.debug
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchLineBreak
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.oneOf
import xyz.lepisma.orgmode.core.parsingError
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.zeroOrMore
import xyz.lepisma.orgmode.lexer.Token

/**
 * A chunk is a block of org mode text that can be of various types as listed here
 */
sealed class OrgChunk: OrgElem {
    data class OrgParagraph(
        val items: List<OrgInlineElem>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgCommentLine(
        val text: String,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgHorizontalLine(
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgTable(
        val dim: Pair<Int, Int>,
        val header: OrgTableRow?,
        val subtables: List<List<OrgTableRow>>,
        val formulaLine: String,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem
}

val parseHorizontalRule: Parser<OrgChunk.OrgHorizontalLine> = matchToken {
    it is Token.HorizontalRule
}.map { output ->
    OrgChunk.OrgHorizontalLine(tokens = output.tokens)
}

val parseParagraph: Parser<OrgChunk.OrgParagraph> = parseInlineElems { trail ->
    val currentToken = trail.last()
    val prevTokens = trail.dropLast(1)

    // Firstly, we check for last token
    when (currentToken) {
        is Token.EOF,
        is Token.UnorderedListMarker,
        is Token.OrderedListMarker,
        is Token.HeadingStars -> return@parseInlineElems true

        is Token.BlockEnd -> when (currentToken.type) {
            Token.BlockType.ASIDE,
            Token.BlockType.SRC,
            Token.BlockType.PAGE_INTRO,
            Token.BlockType.EDITS,
            Token.BlockType.QUOTE,
            Token.BlockType.VERSE -> return@parseInlineElems true

            else -> { }
        }

        is Token.BlockStart -> when (currentToken.type) {
            Token.BlockType.ASIDE,
            Token.BlockType.SRC,
            Token.BlockType.PAGE_INTRO,
            Token.BlockType.EDITS,
            Token.BlockType.QUOTE,
            Token.BlockType.VERSE -> return@parseInlineElems true

            else -> { }
        }

        else -> { }
    }

    // If still not stopped, we check if we have hit 2 or more consecutive linebreaks
    return@parseInlineElems (trail.size > 1 &&
            currentToken is Token.LineBreak &&
            prevTokens.last() is Token.LineBreak)
}.map {
    OrgChunk.OrgParagraph(
        items = it,
        tokens = collectTokens(it)
    )
}

val parseChunk: Parser<OrgChunk> = seq(
    oneOf(
        // ::parseCommentLine,
        parseHorizontalRule,
        // ::parseTable,
        // ::parseCommentBlock,
        // ::parseExampleBlock,
        parseSourceBlock,
        parseQuoteBlock,
        // ::parseCenterBlock,
        // ::parseHTMLBlock,
        parseVerseBlock,
        // ::parseLaTeXBlock,
        parsePageIntroBlock,
        parseEditsBlock,
        parseAsideBlock,
        // ::parseVideoBlock,
        ::parseUnorderedList,
        ::parseOrderedList,
        parseParagraph
    ),
    zeroOrMore(matchLineBreak)
).map { (chunk, lbs) ->
    chunk.tokens = collectTokens(chunk, lbs)
    chunk
}
