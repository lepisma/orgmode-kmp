package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.collectUntil
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchLineBreak
import xyz.lepisma.orgmode.core.matchSpace
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.oneOf
import xyz.lepisma.orgmode.core.oneOrMore
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.zeroOrMore
import xyz.lepisma.orgmode.lexer.Token

sealed class OrgBlock: OrgElem {
    data class OrgCommentBlock(
        val text: String,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgExampleBlock(
        val text: String,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgSourceBlock(
        val language: String,
        val switches: List<String>,
        val headerArgs: List<String>,
        val body: String,
        val name: String?,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgQuoteBlock(
        val body: List<OrgChunk>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgCenterBlock(
        val body: List<OrgChunk>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgHTMLBlock(
        val body: String,
        val name: String?,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgVerseBlock(
        val body: String,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgLaTeXBlock(
        val body: String,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgPageIntroBlock(
        val body: List<OrgChunk>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgEditsBlock(
        val body: List<OrgChunk>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgAsideBlock(
        val body: List<OrgChunk>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgVideoBlock(
        val body: List<OrgChunk>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem
}

val parseSourceBlock: Parser<OrgBlock.OrgSourceBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.SRC },
    matchSpace,
    // This is provisional
    parseOrgLine,
    matchLineBreak,
    collectUntil { it is Token.BlockEnd && it.type == Token.BlockType.SRC },
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.SRC }
).map { (start, sp, configLine, lb, tokens, end) ->
    val allTokens = collectTokens(start, sp, configLine, lb) + tokens + collectTokens(end)

    OrgBlock.OrgSourceBlock(
        language = configLine
            .items
            .filter { it is OrgInlineElem.Text }
            .joinToString("") { (it as OrgInlineElem.Text).text },
        switches = emptyList(),
        headerArgs = emptyList(),
        name = null,
        body = tokens.dropLast(1).joinToString("") { tok -> tok.text },
        tokens = allTokens
    )
}

val parseQuoteBlock: Parser<OrgBlock.OrgQuoteBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.QUOTE },
    matchLineBreak,
    oneOrMore(
        seq(
            oneOf(
                // ::parseCommentLine,
                parseHorizontalRule,
                parseSourceBlock,
                ::parseUnorderedList,
                ::parseOrderedList,
                parseParagraph
            ),
            zeroOrMore(matchLineBreak)
        )
    ),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.QUOTE }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(start, lb, chunks, end)

    OrgBlock.OrgQuoteBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parsePageIntroBlock: Parser<OrgBlock.OrgPageIntroBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.PAGE_INTRO },
    matchLineBreak,
    oneOrMore(
        seq(
            oneOf(
                // ::parseCommentLine,
                parseHorizontalRule,
                parseSourceBlock,
                parseQuoteBlock,
                ::parseUnorderedList,
                ::parseOrderedList,
                parseParagraph
            ),
            zeroOrMore(matchLineBreak)
        )
    ),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.PAGE_INTRO }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(start, lb, chunks, end)

    OrgBlock.OrgPageIntroBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parseVerseBlock: Parser<OrgBlock.OrgVerseBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.VERSE },
    matchLineBreak,
    oneOrMore(
        seq(
            parseParagraph,
            zeroOrMore(matchLineBreak)
        ).map { it.first }),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.VERSE }
).map { (start, lb, paragraphs, end) ->
    val allTokens = collectTokens(start, lb, paragraphs, end)

    OrgBlock.OrgVerseBlock(
        body = paragraphs.joinToString("\n\n") { p -> p.tokens.joinToString("") { it.text } },
        tokens = allTokens
    )
}

val parseAsideBlock: Parser<OrgBlock.OrgAsideBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.ASIDE },
    matchLineBreak,
    oneOrMore(
        seq(
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
                // ::parseLaTeXBlock
                // ::parseVideoBlock,
                ::parseUnorderedList,
                ::parseOrderedList,
                parseParagraph
            ),
            zeroOrMore(matchLineBreak)
        )
    ),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.ASIDE }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(start, lb, chunks, end)

    OrgBlock.OrgAsideBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}

val parseEditsBlock: Parser<OrgBlock.OrgEditsBlock> = seq(
    matchToken { it is Token.BlockStart && it.type == Token.BlockType.EDITS },
    matchLineBreak,
    oneOrMore(
        seq(
            oneOf(
                // ::parseCommentLine,
                parseHorizontalRule,
                parseSourceBlock,
                parseQuoteBlock,
                ::parseUnorderedList,
                ::parseOrderedList,
                parseParagraph
            ),
            zeroOrMore(matchLineBreak)
        )
    ),
    matchToken { it is Token.BlockEnd && it.type == Token.BlockType.EDITS }
).map { (start, lb, chunks, end) ->
    val allTokens = collectTokens(start, lb, chunks, end)

    OrgBlock.OrgEditsBlock(
        body = chunks.map { it.first as OrgChunk },
        tokens = allTokens
    )
}