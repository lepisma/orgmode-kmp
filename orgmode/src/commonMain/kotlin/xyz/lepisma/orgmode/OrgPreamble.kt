package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token
import kotlinx.datetime.LocalDate

/**
 * Preamble is everything that comes in the start before the actual content starts
 */
data class OrgPreamble(
    val title: OrgLine,
    val author: String? = null,
    val email: String? = null,
    val date: LocalDate? = null,
    val category: String? = null,
    val filetags: OrgTags? = null,
    val tags: OrgTags? = null,  // NOTE: I use this wrongly in pile
    val options: OrgOptions? = null,
    val pile: OrgOptions? = null,
    val properties: OrgProperties? = null,
    override var tokens: List<Token>
) : OrgElem