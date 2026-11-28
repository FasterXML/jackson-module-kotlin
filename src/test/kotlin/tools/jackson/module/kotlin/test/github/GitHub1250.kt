package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.ValueDeserializerModifier
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHub1250 {
    // WrapsAnyValueClassBoxDeserializer
    @JvmInline
    value class Amount(val value: BigDecimal)

    @JvmInline
    value class Day(val value: LocalDate)

    @JvmInline
    value class NullableAmount(val value: BigDecimal?)

    // NoConversionCreatorBoxDeserializer.WrapsJavaUuid
    @JvmInline
    value class Id(val value: UUID)

    // HasConversionCreatorWrapsSpecifiedBoxDeserializer
    @JvmInline
    value class Converted(val value: Int) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun of(value: BigDecimal): Converted = Converted(value.toInt())
        }
    }

    // Wrapped type refers back to the value class
    @JvmInline
    value class Node(val value: Tree)

    data class Tree(val children: List<Node>)

    data class Dto(
        val amount: Amount,
        val day: Day,
        val nullableAmount: NullableAmount,
        val nullableProperty: Amount?,
        val id: Id,
        val converted: Converted,
        val amounts: List<Amount>,
        val days: Map<String, Day>,
    )

    private fun json(i: Int) = """
        {
          "amount": $i.50,
          "day": "2024-01-${(i % 28 + 1).toString().padStart(2, '0')}",
          "nullableAmount": null,
          "nullableProperty": null,
          "id": "00000000-0000-0000-0000-${i.toString().padStart(12, '0')}",
          "converted": $i.9,
          "amounts": [1.0, $i],
          "days": { "a": "2024-02-29" }
        }
    """.trimIndent()

    private fun expected(i: Int) = Dto(
        Amount(BigDecimal("$i.50")),
        Day(LocalDate.of(2024, 1, i % 28 + 1)),
        NullableAmount(null),
        null,
        Id(UUID.fromString("00000000-0000-0000-0000-${i.toString().padStart(12, '0')}")),
        Converted(i),
        listOf(Amount(BigDecimal("1.0")), Amount(BigDecimal(i))),
        mapOf("a" to Day(LocalDate.of(2024, 2, 29))),
    )

    private val input = (0 until 10).joinToString(",", "[", "]") { json(it) }

    // Counts how often a deserializer is constructed (= DeserializerCache misses), per type
    class ConstructionCounter : ValueDeserializerModifier() {
        val counts = mutableMapOf<String, Int>()

        @Synchronized
        override fun modifyDeserializer(
            config: DeserializationConfig,
            beanDescRef: BeanDescription.Supplier,
            deserializer: ValueDeserializer<*>,
        ): ValueDeserializer<*> {
            counts.merge(beanDescRef.type.rawClass.simpleName, 1, Int::plus)
            return deserializer
        }

        @Synchronized
        fun snapshot(): Map<String, Int> = counts.toMap()
    }

    @Test
    fun wrappedDeserializersAreNotConstructedPerValue() {
        val counter = ConstructionCounter()
        val mapper = jacksonMapperBuilder()
            .addModule(SimpleModule().setDeserializerModifier(counter))
            .build()

        assertEquals((0 until 10).map { expected(it) }, mapper.readValue<List<Dto>>(input))
        val afterFirstRead = counter.snapshot()
        assertEquals((0 until 10).map { expected(it) }, mapper.readValue<List<Dto>>(input))

        assertEquals(afterFirstRead, counter.snapshot())
    }

    @Test
    fun values() {
        assertEquals(expected(3), defaultMapper.readValue<Dto>(json(3)))
        assertEquals(Amount(BigDecimal("1.50")), defaultMapper.readValue<Amount>("1.50"))
        assertEquals(Day(LocalDate.of(2024, 1, 2)), defaultMapper.readValue<Day>("\"2024-01-02\""))
        assertEquals(NullableAmount(BigDecimal.TEN), defaultMapper.readValue<NullableAmount>("10"))
        assertEquals(Converted(1), defaultMapper.readValue<Converted>("1.5"))
        assertEquals(
            listOf(Id(UUID(0, 1)), Id(UUID(0, 2))),
            defaultMapper.readValue<List<Id>>("""["00000000-0000-0000-0000-000000000001","00000000-0000-0000-0000-000000000002"]""")
        )
        assertEquals(listOf(null, Amount(BigDecimal.ONE)), defaultMapper.readValue<List<Amount?>>("[null, 1]"))
    }

    @Test
    fun recursiveWrappedType() {
        val leaf = Node(Tree(emptyList()))
        assertEquals(
            Node(Tree(listOf(leaf, Node(Tree(listOf(leaf)))))),
            defaultMapper.readValue<Node>("""{"children":[{"children":[]},{"children":[{"children":[]}]}]}""")
        )
    }
}
