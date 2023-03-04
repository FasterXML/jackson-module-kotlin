package tools.jackson.module.kotlin

import tools.jackson.core.PrettyPrinter
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.ObjectWriter

// This `printer` is used to match the output from Jackson to the newline char of the source code.
// If this is removed, comparisons will fail in a Windows-like platform.
val LF_PRINTER: PrettyPrinter =
    DefaultPrettyPrinter().withObjectIndenter(DefaultIndenter().withLinefeed("\n"))

fun ObjectMapper.testPrettyWriter(): ObjectWriter = this.writer().with(LF_PRINTER)
