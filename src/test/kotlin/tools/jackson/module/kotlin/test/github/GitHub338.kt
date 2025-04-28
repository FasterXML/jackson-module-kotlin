package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonRootName
import tools.jackson.databind.DeserializationFeature
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.kotlinModule
import kotlin.test.assertEquals

class GitHub338 {
    data class Properties(
        @JacksonXmlProperty(localName = "NEW_DATE")
        val newDate: String?,
        @JacksonXmlProperty(localName = "BC_1MONTH")
        val oneMonth: String?
    )

    data class Content(
        val properties: Properties
    )

    data class Entry(
        val id: String,
        val updated: String,
        val content: Content
    )

    @JsonRootName(namespace = "http://www.w3.org/2005/Atom", value = "feed")
    data class Feed @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
        @JacksonXmlElementWrapper(useWrapping = false)
        val entry: List<Entry>
    )

    val xml = """
        <feed xml:base="http://data.treasury.gov/Feed.svc/" xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices" xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" xmlns="http://www.w3.org/2005/Atom">
          <entry>
            <id>http://data.treasury.gov/Feed.svc/DailyTreasuryYieldCurveRateData(1)</id>
            <updated>2020-05-08T22:36:11Z</updated>
            <content type="application/xml">
              <m:properties>
                <d:NEW_DATE m:type="Edm.DateTime">1997-01-02T00:00:00</d:NEW_DATE>
                <d:BC_1MONTH m:type="Edm.Double" m:null="true" />
              </m:properties>
            </content>
          </entry>
          <entry>
            <id>http://data.treasury.gov/Feed.svc/DailyTreasuryYieldCurveRateData(2)</id>
            <updated>2020-05-08T22:36:11Z</updated>
            <content type="application/xml">
              <m:properties>
                <d:NEW_DATE m:type="Edm.DateTime">1996-12-31T00:00:00</d:NEW_DATE>
                <d:BC_1MONTH m:type="Edm.Double" m:null="true" />
              </m:properties>
            </content>
          </entry>
        </feed>
    """.trimIndent()

    @Test
    fun test() {
        val mapper = XmlMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(kotlinModule())
            .build()
        val expected = Feed(
            listOf(
                Entry(
                    "http://data.treasury.gov/Feed.svc/DailyTreasuryYieldCurveRateData(1)",
                    "2020-05-08T22:36:11Z",
                    Content(Properties("1997-01-02T00:00:00", ""))
                ),
                Entry(
                    "http://data.treasury.gov/Feed.svc/DailyTreasuryYieldCurveRateData(2)",
                    "2020-05-08T22:36:11Z",
                    Content(Properties("1996-12-31T00:00:00", ""))
                )
            )
        )
        val actual = mapper.readValue<Feed>(xml)

        assertEquals(expected, actual)
    }
}
