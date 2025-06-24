package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.collectUntil
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.lexer.Token

/**
 * A single line string with Org Mode formatting enabled
 */
data class OrgLine(
    val items: List<OrgInlineElem>,
    override var tokens: List<Token>
) : OrgElem

val parseOrgLine = collectUntil { it is Token.LineBreak || it is Token.EOF }
    .map { tokens ->
        OrgLine(
            items = buildInlineElems(tokens),
            tokens = tokens
        )
    }