package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithub145 {
    class Person1(@JsonProperty("preName") val preName:String, @JsonProperty("lastName") val lastName:String)
    {
        constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))
    }

    class PersonGood1(@JsonProperty("preName") val preName:String, @JsonProperty("lastName") val lastName:String)
    {
        @JsonCreator constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))
    }

    class PersonGood2(val preName:String, val lastName:String)
    {
        @JsonCreator constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))
    }

    class PersonGood3 private constructor (val preName:String, val lastName:String)
    {
        @JsonCreator constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))
    }

    class PersonGood4(preNameAndLastName: String) {
        val preName:String
        val lastName:String

        init {
            this.preName = preNameAndLastName.substringBefore(",")
            this.lastName = preNameAndLastName.substringAfter(",")
        }
    }

    class PersonGood5 @JsonCreator constructor(@JsonProperty("preName") val preName:String, @JsonProperty("lastName") val lastName:String)
    {
        @JsonCreator constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))
    }

    class PersonGood6 private constructor(val preName:String, val lastName:String)
    {
        private constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))

        companion object {
            @JsonCreator @JvmStatic fun createFromJson(preNameAndLastName: String): PersonGood6
            {
                return PersonGood6(preNameAndLastName)
            }

            @JsonCreator  @JvmStatic fun createFromData(preName: String, lastName: String): PersonGood6 {
                return PersonGood6(preName, lastName)
            }
        }
    }


    class PersonGood7 constructor(val preName:String, val lastName:String)
    {
        private constructor(preNameAndLastName:String): this(preNameAndLastName.substringBefore(","),preNameAndLastName.substringAfter(","))

        companion object {
            @JsonCreator @JvmStatic fun createFromJson(preNameAndLastName: String): PersonGood7
            {
                return PersonGood7(preNameAndLastName)
            }
        }
    }


    @Test
    fun workingTestWithoutKotlinModule()
    {
        val objectMapper = ObjectMapper()
        val personA = objectMapper.readValue("""{"preName":"TestPreName","lastName":"TestLastname"}""", Person1::class.java)
        val personB = objectMapper.readValue(""""TestPreName,TestLastname"""", Person1::class.java)
    }

    @Test
    fun testOtherVariationsOfSingleAndDataConstructors()
    {
        val objectMapper = jacksonObjectMapper()

        val personGood1String = objectMapper.readValue<PersonGood1>(""""TestPreName,TestLastname"""")
        val personGood1Json = objectMapper.readValue<PersonGood1>("""{"preName":"TestPreName","lastName":"TestLastname"}""")

        val personGood2String  = objectMapper.readValue<PersonGood2>(""""TestPreName,TestLastname"""")
        val personGood2Json = objectMapper.readValue<PersonGood2>("""{"preName":"TestPreName","lastName":"TestLastname"}""")

        val personGood3String = objectMapper.readValue<PersonGood3>(""""TestPreName,TestLastname"""")
        val personGood3Json = objectMapper.readValue<PersonGood3>("""{"preName":"TestPreName","lastName":"TestLastname"}""")

        val personGood4String = objectMapper.readValue<PersonGood4>(""""TestPreName,TestLastname"""")
        // person4 does not have parameter bound constructor, only string

        val personGood5String = objectMapper.readValue<PersonGood5>(""""TestPreName,TestLastname"""")
        val personGood5Json = objectMapper.readValue<PersonGood5>("""{"preName":"TestPreName","lastName":"TestLastname"}""")

        val personGood6String = objectMapper.readValue<PersonGood6>(""""TestPreName,TestLastname"""")
        val personGood6Json = objectMapper.readValue<PersonGood6>("""{"preName":"TestPreName","lastName":"TestLastname"}""")

        val personGood7String = objectMapper.readValue<PersonGood7>(""""TestPreName,TestLastname"""")
        val personGood7Json = objectMapper.readValue<PersonGood7>("""{"preName":"TestPreName","lastName":"TestLastname"}""")

    }
}