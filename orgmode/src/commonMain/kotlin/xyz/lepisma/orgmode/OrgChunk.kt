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

val parseParagraph: Parser<OrgChunk.OrgParagraph> = Parser { tokens, pos ->
    if (pos >= tokens.size) {
        return@Parser parsingError("Exhausted tokens while parsing paragraph")
    }

    // Stopping condition for paragraph parsing
    fun shouldStop(position: Int): Boolean {
        val token = tokens[position]

        return when (token) {
            is Token.EOF,
            is Token.UnorderedListMarker,
            is Token.OrderedListMarker,
            is Token.HeadingStars -> true

            is Token.BlockEnd -> when (token.type) {
                Token.BlockType.ASIDE,
                Token.BlockType.SRC,
                Token.BlockType.PAGE_INTRO,
                Token.BlockType.EDITS,
                Token.BlockType.QUOTE,
                Token.BlockType.VERSE -> true

                else -> false
            }

            is Token.BlockStart -> when (token.type) {
                Token.BlockType.ASIDE,
                Token.BlockType.SRC,
                Token.BlockType.PAGE_INTRO,
                Token.BlockType.EDITS,
                Token.BlockType.QUOTE,
                Token.BlockType.VERSE -> true

                else -> false
            }

            else -> false
        }
    }

    var accumulator = mutableListOf<Token>()
    var currentPos = pos
    var lbCount = 0
    var tok: Token

    while (currentPos < tokens.size) {
        tok = tokens[currentPos]

        if (shouldStop(currentPos)) {
            break
        }

        lbCount = if (tok is Token.LineBreak) {
            lbCount + 1
        } else {
            0
        }
        if (lbCount == 2) {
            accumulator.dropLast(1)
            break
        }

        accumulator.add(tokens[currentPos])
        currentPos++
    }

    if (accumulator.isNotEmpty()) {
        // Currently taking all raw texts from tokens and throwing them as a single text
        ParsingResult.Success(
            output = OrgChunk.OrgParagraph(
                items = buildInlineElems(accumulator),
                tokens = accumulator
            ),
            nextPos = currentPos
        )
    } else {
        parsingError(
            "Unable to parse paragraph because of lack of tokens",
            tokens = listOf(tokens[pos])
        )
    }
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
    chunk as OrgChunk
}
