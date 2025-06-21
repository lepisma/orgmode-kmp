package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.lexer.Token

/**
 * Any parsed org element which keeps tracks of the tokens used in parsing
 */
sealed interface OrgElem {
    // We allow changing tokens here so that tree manipulation is easy
    var tokens: List<Token>
}

/**
 * Options that could go in top of the file, code block headers, etc.
 */
data class OrgOptions(
    val map: Map<String, String>,
    override var tokens: List<Token>
) : OrgElem

/**
 * Tags for files or headings or anywhere else
 */
data class OrgTags(
    val tags: List<String>,
    override var tokens: List<Token>
) : OrgElem

data class OrgTableRow(
    val cells: List<OrgLine>,
    override var tokens: List<Token>
) : OrgElem

/**
 * Represents parsing error of all kinds
 */
data class OrgParsingError(
    val message: String,
    override var tokens: List<Token> = emptyList()
) : OrgElem

/**
 * Represents a plain token parse
 */
data class OrgToken(
    override var tokens: List<Token>
) : OrgElem