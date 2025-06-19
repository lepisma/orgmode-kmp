package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token

/**
 * Preface contains the chunks before first heading
 */
data class OrgPreface(
    val body: List<OrgChunk>,
    override var tokens: List<Token>
) : OrgElem