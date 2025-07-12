# Module orgmode

Kotlin Multiplatform library for parsing, formatting, and working with [Org
Mode](https://orgmode.org/) files and content.

The parser is based on parser combinators and you can compose them to make new or variations of 
existing parsers. Every parser returns an element of type [OrgElem](xyz.lepisma.orgmode.OrgElem) 
that also tracks the tokens ([Token](xyz.lepisma.orgmode.lexer.Token)) used in making the element. 
Each token, in turn, tracks its original position in the string. This allows you to completely go
from the parse tree to tokens and further to the string.

You can make changes in the tree and tokens and then generate a new string. To do this easier, you
might want to work with optics using something like [Arrow](https://arrow-kt.io/learn/immutable-data/).
Note that while you can change the tokens attached to tree elements easily, changing and updating
the text positions for tokens is still manual and could be error-prone. You can keep stale text
positions for many common use cases since tokens to string doesn't rely on text positions. A later
version of this library will ensure easier operation on positions.

## Usage
There are two stages for using the parser:

1. Lexing converts an org string to individual [tokens](xyz.lepisma.orgmode.Token).
2. Parsing converts a list of tokens to any [org element](xyz.lepisma.orgmode.OrgElem).

The root element of a full document is [OrgDocument](xyz.lepisma.orgmode.OrgDocument) which can be
parsed like this:

```kotlin
val tokens: List<Token> = OrgLexer(orgString).tokenize()
val document: OrgDocument? = parse(tokens)
```

Each [OrgElem](xyz.lepisma.orgmode.OrgElem) has, usually, its own parser that can be invoked on
tokens that build that element up. For using them, you will also be helped by the parser combinators
[here](xyz.lepisma.orgmode.core). As an example, here is how to parse a line with an org link:

```kotlin
import xyz.lepisma.orgmode.lexer.OrgLexer
import xyz.lepisma.orgmode.core.seq
import xyz.lepisma.orgmode.core.matchSOF
import xyz.lepisma.orgmode.core.matchEOF
import xyz.lepisma.orgmode.parseOrgLine

val text = "this is [[attachment:hello world.pdf]]"
val tokens = OrgLexer(text).tokenize()
val parser = seq(matchSOF, parseOrgLine, matchEOF)

// Output is a triple with items matching SOF OrgToken, OrgLine, and EOF OrgToken
val line = (parser.invoke(tokens, 0) as ParsingResult.Success).output.second

// line.items.size shouldBe 5
// (line.items.last() is OrgInlineElem.Link) shouldBe true
// (line.items.last() as OrgInlineElem.Link).title shouldBe null
// (line.items.last() as OrgInlineElem.Link).target shouldBe "hello world.pdf"
// (line.items.last() as OrgInlineElem.Link).type shouldBe "attachment"
```

### Going back
For going from an element to tokens, use the `tokens` property of the element.  For further going 
from tokens to raw string, call [inverseLex](xyz.lepisma.orgmode.lexer.inverseLex) on the list of
tokens.

```kotlin
// The following will be true in all cases
val reconstructedString = inverseLex(unparse(parse(OrgLexer(orgString).tokenize())!!))
reconstructedString shouldBe orgString
```

## Supported OrgMode Features
The parser is not complete yet, but here are the currently supported elements (lexer capabilities
follow whatever is needed for the parser):

1. Properties blocks, both at document level and under section headings.
2. Document preamble, tags, configs etc. This is not complete but the basic items like title are supported.
3. Ordered, unordered, nested lists with checkboxes.
4. Inside org text, only links, datetime stamps, and datetime ranges are supported. Other markup support is on the way.
5. #hashtags and #hashmetric(value). Note that these are not standard features of Org Mode but I find them helpful.
6. Horizontal line
7. Source, Quote, and Verse blocks
8. Page Intro, Edits, Aside blocks. These are additional features, not present in Org Mode.
9. Headings with tags and planning info

Notable missing items that will be added before calling this parser complete:
1. Tables
2. Inline markups like bold, italic, etc.
3. LaTeX elements
4. Heading priority and todo state

Other features like footnotes, citations, and anything else missing will be added on a need basis.

### Combinators
The list of parser combinators is relatively complete and can be checked out [here](xyz.lepisma.orgmode.core).

## Development
Development documentation will be added once the library is stabilized.
