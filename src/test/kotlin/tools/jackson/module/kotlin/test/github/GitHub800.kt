package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ObjectReader
import tools.jackson.databind.module.SimpleModule
import tools.jackson.dataformat.csv.CsvMapper
import tools.jackson.dataformat.csv.CsvSchema
import tools.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Test
import tools.jackson.dataformat.csv.CsvReadFeature
import kotlin.test.assertEquals

class GitHub800 {
    val CSV = """
        BAR_HEADER,BAZ_HEADER
        bar1,baz1
    """.trimIndent()

    val MAPPER =
        CsvMapper
            .builder()
            .addModule(
                SimpleModule()
                    .addDeserializer(BazValueClass::class.java, object : ValueDeserializer<BazValueClass>() {
                        override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = BazValueClass(p.valueAsString)
                    })
                    .addDeserializer(BazDataClass::class.java, object : ValueDeserializer<BazDataClass>() {
                        override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = BazDataClass(p.valueAsString)
                    })
            )
            .addModule(kotlinModule())
            .build()

    inline fun <reified T> createSchema(columnReordering: Boolean): CsvSchema =
        MAPPER
            .schemaFor(T::class.java)
            .withColumnReordering(columnReordering)
            .withHeader()

    inline fun <reified T> createReader(columnReordering: Boolean): ObjectReader =
        MAPPER
            .readerFor(T::class.java)
            .with(CsvReadFeature.WRAP_AS_ARRAY)
            .with(createSchema<T>(columnReordering))

    data class FooWithValueClass(
        @field:JsonProperty("BAR_HEADER")
        val bar: String?,
        @field:JsonProperty("BAZ_HEADER")
        val baz: BazValueClass?,
    )

    data class FooWithDataClass(
        @field:JsonProperty("BAR_HEADER")
        val bar: String?,
        @field:JsonProperty("BAZ_HEADER")
        val baz: BazDataClass?,
    )

    @JvmInline
    value class BazValueClass(val value: String)

    data class BazDataClass(val value: String)

    @Test
    fun valueClassWithoutColumnReordering() {
        val actual = createReader<FooWithValueClass>(false)
            .readValues<FooWithValueClass>(CSV)
            .readAll()

        assertEquals(listOf(FooWithValueClass("bar1", BazValueClass("baz1"))), actual)
    }

    @Test
    fun valueClassWithColumnReordering() {
        val actual = createReader<FooWithValueClass>(true)
            .readValues<FooWithValueClass>(CSV)
            .readAll()

        assertEquals(listOf(FooWithValueClass("bar1", BazValueClass("baz1"))), actual)
    }

    @Test
    fun dataClassWithoutColumnReordering() {
        val actual = createReader<FooWithDataClass>(false)
            .readValues<FooWithDataClass>(CSV)
            .readAll()

        assertEquals(listOf(FooWithDataClass("bar1", BazDataClass("baz1"))), actual)
    }

    @Test
    fun dataClassWithColumnReordering() {
        val actual = createReader<FooWithDataClass>(true)
            .readValues<FooWithDataClass>(CSV)
            .readAll()

        assertEquals(listOf(FooWithDataClass("bar1", BazDataClass("baz1"))), actual)
    }
}
