package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.collectTokens
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

/**
 * Parse a single line of org inline elements
 */
val parseOrgLine = parseInlineElems { trail ->
    val currentTok = trail.last()
    currentTok is Token.LineBreak || currentTok is Token.EOF
}.map {
    OrgLine(items = it, tokens = collectTokens(it))
}