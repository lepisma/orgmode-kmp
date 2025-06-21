package xyz.lepisma.orgmode

import xyz.lepisma.orgmode.core.Parser
import xyz.lepisma.orgmode.core.ParsingResult
import xyz.lepisma.orgmode.core.collectTokens
import xyz.lepisma.orgmode.core.debug
import xyz.lepisma.orgmode.core.map
import xyz.lepisma.orgmode.core.matchEOF
import xyz.lepisma.orgmode.core.matchSOF
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.zeroOrMore
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

/**
 * Merge partition in one super-section
 */
private fun mergePartition(partition: List<OrgSection>): OrgSection {
    val superSection = partition.first()
    var restPartitions = partitionSections(partition.drop(1))

    return if (restPartitions.isEmpty()) {
        superSection
    } else {
        val chunks = superSection.body + restPartitions.map { mergePartition(it) }
        superSection.copy(
            body = chunks,
            tokens = collectTokens(superSection.heading, chunks)
        )
    }
}

/**
 * Partition sections in groups that lie under the same super-section which is the first item of
 * each partition.
 */
private fun partitionSections(sections: List<OrgSection>): List<List<OrgSection>> {
    if (sections.isEmpty()) {
        return emptyList()
    }

    val partitions = mutableListOf<MutableList<OrgSection>>(mutableListOf(sections.first()))
    var pi = 0

    for (section in sections.drop(1)) {
        if (section.heading.level.level > partitions[pi].first().heading.level.level) {
            // We are going to bury this under the super-section
            partitions[pi].add(section)
        } else {
            // We will create a new partition
            partitions.add(mutableListOf(section))
            // The current partition index also has to advance
            pi++
        }
    }

    return partitions
}


/**
 * Nest a sequential list of sections from the document in proper hierarchy
 */
private fun nestSections(sections: List<OrgSection>): List<OrgSection> {
    if (sections.isEmpty()) {
        return emptyList()
    }
    return partitionSections(sections).map { mergePartition(it) }
}

val parseDocument: Parser<OrgDocument> = seq(
    matchSOF,
    parsePreamble,
    parsePreface,
    zeroOrMore(parseSection),
    matchEOF
).map { (sof, preamble, preface, sections, eof) ->
    // Sections are parsed independently without any nesting. Here we will nest them before creating
    // the final document
    OrgDocument(
        preamble = preamble,
        preface = preface,
        content = nestSections(sections),
        tokens = collectTokens(sof, preamble, preface, sections, eof)
    )
}

/**
 * The main exposed function for parsing an org mode document
 */
fun parse(tokens: List<Token>): OrgDocument? {
    val result = parseDocument.invoke(tokens, pos = 0)
    return if (result is ParsingResult.Success) {
        result.output
    } else {
        null
    }
}

/**
 * Convert a document back to the list to tokens
 */
fun unparse(document: OrgDocument): List<Token> {
    return document.tokens
}