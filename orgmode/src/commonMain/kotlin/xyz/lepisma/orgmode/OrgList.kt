package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.ParsingResult
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.lazy
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchLineBreak
import xyz.lepisma.orgmode.core.matchSpace
import xyz.lepisma.orgmode.core.matchToken
import xyz.lepisma.orgmode.core.maybe
import xyz.lepisma.orgmode.core.oneOf
import xyz.lepisma.orgmode.core.oneOrMore
import xyz.lepisma.orgmode.core.repeat
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.zeroOrMore
import xyz.lepisma.orgmode.lexer.Token

/**
 * Org list which could have nested lists inside or list content
 */
sealed class OrgList {
    data class OrgUnorderedList(
        val markerStyle: OrgUnorderedListMarker,
        val items: List<OrgList>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgOrderedList(
        val markerStyle: OrgOrderedListMarker,
        val items: List<OrgList>,
        override var tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgListItem(
        val content: List<OrgChunk>,
        val checkbox: OrgListCheckState?,
        override var tokens: List<Token>
    ) : OrgList(), OrgElem

    data class OrgDescriptionListItem(
        val term: String,
        val description: OrgChunk.OrgParagraph,
        override var tokens: List<Token>
    ) : OrgList(), OrgElem
}

enum class OrgUnorderedListMarker {
    PLUS,
    DASH
}

enum class OrgOrderedListMarker {
    PERIOD,
    PARENTHESIS
}

enum class OrgListCheckState {
    CHECKED, UNCHECKED, PARTIAL
}

/**
 * This starts after the checkbox, if present, in each list item.
 */
fun parseListItemChunks(indentLevel: Int): Parser<List<OrgChunk>> {
    return seq(
        // First paragraph match would be without any indent since we start right at the
        // beginning of the paragraph
        parseParagraph,
        zeroOrMore(matchLineBreak),
        zeroOrMore(
            seq(
                oneOf(
                    lazy { unorderedList(indentLevel + 1) },
                    lazy { orderedList(indentLevel + 1) },
                    seq(
                        // match > (indentLevel + 1) * 2 spaces
                        repeat(min = (indentLevel + 1) * 2, max = null, matchSpace),
                        parseParagraph
                    ).map { (indent, p) ->
                        p.tokens = collectTokens(indent, p)
                        p
                    }
                ),
                zeroOrMore(matchLineBreak)
            )
        )
    ).map { (firstChunk, lbs, restItems) ->
        firstChunk.tokens = collectTokens(firstChunk, lbs)
        listOf(firstChunk) + restItems.map {
            val chunk = it.first as OrgChunk
            chunk.tokens = collectTokens(it)
            chunk
        }
    }
}

fun unorderedList(indentLevel: Int = 0): Parser<OrgList.OrgUnorderedList> {
    return oneOrMore(
        seq(
            // Unordered lists have indentation similar to ordered lists
            (::matchToken) { it is Token.UnorderedListMarker && it.nIndent >= indentLevel * 2 },
            matchSpace,
            maybe(seq(matchToken { it is Token.CheckBox }, matchSpace)),
            parseListItemChunks(indentLevel)
        )
    ).map { listItems ->
        val markerTok = listItems.first().first
        val markerStyle = when (((markerTok.tokens[0]) as Token.UnorderedListMarker).style) {
            Token.UnorderedListMarkerStyle.DASH -> OrgUnorderedListMarker.DASH
            Token.UnorderedListMarkerStyle.PLUS -> OrgUnorderedListMarker.PLUS
        }

        var items: MutableList<OrgList.OrgListItem> = mutableListOf()

        for ((marker, sp, cb, chunks) in listItems) {
            val checkbox = if (cb == null) {
                null
            } else {
                when ((cb.first.tokens[0] as Token.CheckBox).state) {
                    Token.CheckBoxState.UNCHECKED -> OrgListCheckState.UNCHECKED
                    Token.CheckBoxState.CHECKED -> OrgListCheckState.CHECKED
                    Token.CheckBoxState.PARTIAL -> OrgListCheckState.PARTIAL
                }
            }
            items.add(
                OrgList.OrgListItem(
                    content = chunks,
                    checkbox = checkbox,
                    tokens = collectTokens(marker, sp, cb, chunks)
                )
            )
        }

        OrgList.OrgUnorderedList(
            markerStyle = markerStyle,
            items = items,
            tokens = collectTokens(listItems)
        )
    }
}

fun parseUnorderedList(tokens: List<Token>, pos: Int): ParsingResult<OrgList.OrgUnorderedList> {
    return unorderedList(0).invoke(tokens, pos)
}

fun orderedList(indentLevel: Int = 0): Parser<OrgList.OrgOrderedList> {
    return oneOrMore(
        seq(
            // Ordered lists have a indentation that's dependent on the number of characters in the
            // marker. For say "1.", the indent would be 3, for "11.", the indent would be 4.
            // But I have also seen a fixed indent working out here like in unordered case. We will
            // support both.
            ::matchToken { it is Token.OrderedListMarker && it.nIndent >= indentLevel * 2 },
            matchSpace,
            maybe(seq(matchToken { it is Token.CheckBox }, matchSpace)),
            parseListItemChunks(indentLevel)
        )
    ).map { listItems ->
        val markerTok = listItems.first().first
        val markerStyle = when (((markerTok.tokens[0]) as Token.OrderedListMarker).style) {
            Token.OrderedListMarkerStyle.PERIOD -> OrgOrderedListMarker.PERIOD
            Token.OrderedListMarkerStyle.PARENTHESIS -> OrgOrderedListMarker.PARENTHESIS
        }

        var items: MutableList<OrgList.OrgListItem> = mutableListOf()

        for ((marker, sp, cb, chunks) in listItems) {
            val checkbox = if (cb == null) {
                null
            } else {
                when ((cb.first.tokens[0] as Token.CheckBox).state) {
                    Token.CheckBoxState.UNCHECKED -> OrgListCheckState.UNCHECKED
                    Token.CheckBoxState.CHECKED -> OrgListCheckState.CHECKED
                    Token.CheckBoxState.PARTIAL -> OrgListCheckState.PARTIAL
                }
            }
            items.add(
                OrgList.OrgListItem(
                    content = chunks,
                    checkbox = checkbox,
                    tokens = collectTokens(marker, sp, cb, chunks)
                )
            )
        }

        OrgList.OrgOrderedList(
            markerStyle = markerStyle,
            items = items,
            tokens = collectTokens(listItems)
        )
    }
}

fun parseOrderedList(tokens: List<Token>, pos: Int): ParsingResult<OrgList.OrgOrderedList> {
    return orderedList(0).invoke(tokens, pos)
}