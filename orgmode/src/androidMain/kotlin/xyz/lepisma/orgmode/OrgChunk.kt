package xyz.lepisma.orgmode

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