package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.zeroOrMore
import xyz.lepisma.orgmode.lexer.Token

/**
 * Preface contains the chunks before first heading
 */
data class OrgPreface(
    val body: List<OrgChunk>,
    override var tokens: List<Token>
) : OrgElem

val parsePreface: Parser<OrgPreface> = zeroOrMore(parseChunk).map { output ->
    OrgPreface(
        body = output,
        tokens = collectTokens(output)
    )
}