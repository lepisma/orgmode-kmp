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
 * Unlike simple options, org properties could have full fledged org-mode text
 */
data class OrgProperties(
    val map: Map<String, OrgLine>,
    override var tokens: List<Token>
) : OrgElem

val parseProperties: Parser<OrgProperties> = seq(
    (::matchToken) { it is Token.DrawerStart },
    matchLineBreak,
    oneOrMore(
        seq(
            ::matchToken { it is Token.DrawerPropertyKey },
            zeroOrMore(matchSpace),
            ::matchToken { it is Token.DrawerPropertyValue },
            matchLineBreak
        )
    ),
    ::matchToken { it is Token.DrawerEnd }
).map { (ds, lb, propLines, de) ->
    var map = mutableMapOf<String, OrgLine>()

    for ((k, _, v, _) in propLines) {
        val keyString = (k.tokens.first() as Token.DrawerPropertyKey).key
        val valueString = (v.tokens.first() as Token.DrawerPropertyValue).value

        map[keyString] = OrgLine(
            items = listOf(
                OrgInlineElem.Text(
                    valueString,
                    tokens = v.tokens
                )
            ),
            tokens = v.tokens
        )
    }

    OrgProperties(
        map = map,
        tokens = collectTokens(ds, lb, propLines, de)
    )
}