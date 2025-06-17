package xyz.lepisma.orgmode

import java.time.LocalDate

enum class OrgListType {
    ORDERED,
    UNORDERED
}

/**
 * Any parsed org element which keeps tracks of the tokens used in parsing
 */
sealed interface OrgElem {
    // We allow changing tokens here so that tree manipulation is easy
    var tokens: List<Token>
}

/**
 * Represents org document with all info present 'in' the file.
 */
data class OrgDocument (
    val preamble: OrgPreamble,
    val preface: OrgPreface,
    val content: List<OrgSection>,
    override var tokens: List<Token>
) : OrgElem

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

/**
 * Options that could go in top of the file, code block headers, etc.
 */
data class OrgOptions(
    val map: Map<String, String>,
    override var tokens: List<Token>
) : OrgElem

/**
 * Unlike simple options, org properties could have full fledged org-mode text
 */
data class OrgProperties(
    val map: Map<String, OrgLine>,
    override var tokens: List<Token>
) : OrgElem

/**
 * Tags for files or headings or anywhere else
 */
data class OrgTags(
    val tags: List<String>,
    override var tokens: List<Token>
) : OrgElem

/**
 * Preface contains the chunks before first heading
 */
data class OrgPreface(
    val body: List<OrgChunk>,
    override var tokens: List<Token>
) : OrgElem

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

data class OrgTableRow(
    val cells: List<OrgLine>,
    override var tokens: List<Token>
) : OrgElem

/**
 * A single line string with Org Mode formatting enabled
 */
data class OrgLine(
    val items: List<OrgInlineElem>,
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

fun unfillText(text: String): String {
    val lines = text.lines()
    val pattern = Regex("^(#|:|\\||[+-]|\\d+\\.)")

    val processedLines = mutableListOf<String>()
    var buffer = ""

    for (i in lines.indices) {
        val currentLine = lines[i]
        if (pattern.containsMatchIn(currentLine) || currentLine.isBlank()) {
            if (buffer.isNotEmpty()) {
                processedLines.add(buffer)
                buffer = ""
            }
            processedLines.add(currentLine)
        } else {
            buffer += if (buffer.isEmpty()) currentLine else " $currentLine"
        }
    }

    if (buffer.isNotEmpty()) {
        processedLines.add(buffer)
    }

    return processedLines.joinToString("\n")
}
