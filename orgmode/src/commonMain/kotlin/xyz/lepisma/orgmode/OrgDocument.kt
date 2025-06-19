package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token

/**
 * Represents org document with all info present 'in' the file.
 */
data class OrgDocument (
    val preamble: OrgPreamble,
    val preface: OrgPreface,
    val content: List<OrgSection>,
    override var tokens: List<Token>
) : OrgElem