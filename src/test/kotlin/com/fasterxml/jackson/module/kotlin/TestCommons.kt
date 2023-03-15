package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.PrettyPrinter
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter

// This `printer` is used to match the output from Jackson to the newline char of the source code.
// If this is removed, comparisons will fail in a Windows-like platform.
val LF_PRINTER: PrettyPrinter =
    DefaultPrettyPrinter().withObjectIndenter(DefaultIndenter().withLinefeed("\n"))

fun ObjectMapper.testPrettyWriter(): ObjectWriter = this.writer().with(LF_PRINTER)
