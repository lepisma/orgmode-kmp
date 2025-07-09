# Module orgmode

Kotlin Multiplatform library for parsing, formatting, and working with [Org
Mode](https://orgmode.org/) files and content.

The parser is based on parser combinators. At the highest level, you will be
using something like the following:

```kotlin
val tokens = OrgLexer(orgParserTestText).tokenize()
val document = parse(tokens)
```

If parsed correctly, `document` will have an object of type [xyz.lepisma.orgmode.OrgDocument] which
you can deconstruct and use however you like.

## Supported Features
TBD

## Development
TBD

### Parser Combinators
TBD