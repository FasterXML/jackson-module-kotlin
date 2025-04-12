package tools.jackson.module.kotlin

import tools.jackson.core.PrettyPrinter
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.ObjectWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.test.assertEquals

// This `printer` is used to match the output from Jackson to the newline char of the source code.
// If this is removed, comparisons will fail in a Windows-like platform.
val LF_PRINTER: PrettyPrinter =
    DefaultPrettyPrinter().withObjectIndenter(DefaultIndenter().withLinefeed("\n"))

fun ObjectMapper.testPrettyWriter(): ObjectWriter = this.writer().with(LF_PRINTER)

internal val defaultMapper = jacksonObjectMapper()

internal inline fun <reified T : Any> callPrimaryConstructor(mapper: (KParameter) -> Any? = { it.name }): T =
    T::class.primaryConstructor!!.run {
        val args = parameters.associateWith { mapper(it) }
        callBy(args)
    }

// Function for comparing non-data classes.
internal inline fun <reified T : Any> assertReflectEquals(expected: T, actual: T) {
    T::class.memberProperties.forEach {
        assertEquals(it.get(expected), it.get(actual))
    }
}

internal fun createTempJson(json: String): File {
    val file = File.createTempFile("temp", ".json")
    file.deleteOnExit()
    OutputStreamWriter(
        FileOutputStream(file),
        StandardCharsets.UTF_8
    ).use { writer ->
        writer.write(json)
        writer.flush()
    }
    return file
}
