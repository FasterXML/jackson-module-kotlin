package tools.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.SerializationFeature
import tools.jackson.module.kotlin.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.StringWriter
import java.util.*
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class ParameterNameTests {

    private interface TestFields {
        val name: String
        val age: Int
        val primaryAddress: String
        val wrongName: Boolean
        val createdDt: Date

        fun validate(
            nameField: String = name,
            ageField: Int = age,
            addressField: String = primaryAddress,
            wrongNameField: Boolean = wrongName,
            createDtField: Date = createdDt
        ) {
            assertThat(nameField, equalTo("Frank"))
            assertThat(ageField, equalTo(30))
            assertThat(addressField, equalTo("something here"))
            assertThat(wrongNameField, equalTo(true))
            assertThat(createDtField, equalTo(Date(1477419948000)))
        }
    }

    // 24-Oct-2019, tatu: Note that format of timezone ("+00:00" vs "Z") varies between Jackson 2.x and 3.0
    private val normalCasedJson = """{"name":"Frank","age":30,"primaryAddress":"something here","renamed":true,"createdDt":"2016-10-25T18:25:48.000Z"}"""
    private val pascalCasedJson = """{"Name":"Frank","Age":30,"PrimaryAddress":"something here","Renamed":true,"CreatedDt":"2016-10-25T18:25:48.000Z"}"""

    private val normalCasedMapper = jacksonMapperBuilder()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()

    private val pascalCasedMapper = jacksonMapperBuilder()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
            .build()

    // ==================

    private class DefaultAndSpecificConstructor(
        override var name: String = "",
        override var age: Int = 0
    ) : TestFields {
        @JsonProperty("renamed")
        override var wrongName: Boolean = false

        override var primaryAddress: String = ""
        override var createdDt: Date = Date()
    }

    @Test fun NoFailWithDefaultAndSpecificConstructor() {
        val stateObj = normalCasedMapper.readValue<DefaultAndSpecificConstructor>(normalCasedJson)
        stateObj.validate()
    }

    // ==================

    private class NoFailWithoutJsonCreator(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        val renamed: Boolean,
        override val createdDt: Date
    ) : TestFields {
        @JsonIgnore
        override val wrongName = renamed // here for the test validation only
    }

    @Test fun doNotFailWithoutJsonCreator() {
        val stateObj = normalCasedMapper.readValue<NoFailWithoutJsonCreator>(normalCasedJson)
        stateObj.validate(wrongNameField = stateObj.renamed)
    }

    // ==================

    private data class StateObjectAsDataClassExplicitJsonCreator @JsonCreator constructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        val renamed: Boolean,
        override val createdDt: Date
    ) : TestFields {
        @JsonIgnore
        override val wrongName = renamed // here for the test validation only
    }

    @Test fun testDataClassWithExplicitJsonCreator() {
        // data class with explicit JsonCreator and no parameters with JsonProperty
        val stateObj = normalCasedMapper.readValue<StateObjectAsDataClassExplicitJsonCreator>(normalCasedJson)
        stateObj.validate(wrongNameField = stateObj.renamed)

        val test1out = StringWriter()
        normalCasedMapper.writeValue(test1out, stateObj)

        assertThat(test1out.getBuffer().toString(), equalTo(normalCasedJson))
    }

    // ==================

    private data class StateObjectAsDataClassWithJsonCreatorAndJsonProperty @JsonCreator constructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        @JsonProperty("renamed") override val wrongName: Boolean,
        override val createdDt: Date
    ) : TestFields

    @Test fun testDataClassWithExplicitJsonCreatorAndJsonProperty() {
        // data class with JsonCreator and JsonProperty
        val stateObj = normalCasedMapper
            .readValue<StateObjectAsDataClassWithJsonCreatorAndJsonProperty>(normalCasedJson)
        stateObj.validate()

        val test1out = normalCasedMapper.writeValueAsString(stateObj)
        assertThat(test1out, equalTo(normalCasedJson))
    }

    // ==================

    private class StateObjectAsNormalClass @JsonCreator constructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        @JsonProperty("renamed") override val wrongName: Boolean,
        override val createdDt: Date
    ) : TestFields

    @Test fun testNormalClassWithJsonCreator() {
        // normal class
        val stateObj = normalCasedMapper.readValue<StateObjectAsNormalClass>(normalCasedJson)
        stateObj.validate()
    }

    // ==================

    private class StateObjectWithPartialFieldsInConstructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String
    ) : TestFields {
        @JsonProperty("renamed") override var wrongName: Boolean = false
        override var createdDt: Date by Delegates.notNull()
    }

    @Test fun testNormalClassWithPartialConstructorJsonCreator() {
        // normal class with some fields not in constructor
        val stateObj = normalCasedMapper.readValue<StateObjectWithPartialFieldsInConstructor>(normalCasedJson)
        stateObj.validate()
    }

    // ==================

    private class StateObjectAsDataClassConfusingConstructor constructor(
        @Suppress("UNUSED_PARAMETER") nonField: String?,
        override val name: String,
        @Suppress("UNUSED_PARAMETER") yearOfBirth: Int,
        override val age: Int,
        override val primaryAddress: String,
        @JsonProperty("renamed") override val wrongName: Boolean,
        override val createdDt: Date
    ) : TestFields

    @Test fun testDataClassWithNonFieldParametersInConstructor() {
        // data class with non fields appearing as parameters in constructor, this works but null values or defaults for primitive types are passed to
        // the unrecognized fields in the constructor.  Does not work with default values for parameters, because a null does not get converted to the
        // default.

        val stateObj = normalCasedMapper.readValue<StateObjectAsDataClassConfusingConstructor>(normalCasedJson)
        stateObj.validate()
    }

    // ==================

    @Test fun findingConstructorsWithPascalCasedJson() {
        // pascal cased strategy for JSON, note that explicit named JsonProperty are not renamed and must be exactly the same
        val stateObj = pascalCasedMapper.readValue<StateObjectAsDataClassExplicitJsonCreator>(pascalCasedJson)
        stateObj.validate()

        val test1out = pascalCasedMapper.writeValueAsString(stateObj)
        assertThat(test1out, equalTo(pascalCasedJson))
    }


    // ==================

    private class StateObjectWithFactory private constructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        override val wrongName: Boolean,
        override val createdDt: Date
    ) : TestFields {
        var factoryUsed: Boolean = false
        companion object {
            @JvmStatic @JsonCreator fun create(
                @JsonProperty("name") nameThing: String,
                @JsonProperty("age") age: Int,
                @JsonProperty("primaryAddress") primaryAddress: String,
                @JsonProperty("renamed") wrongName: Boolean,
                @JsonProperty("createdDt") createdDt: Date
            ): StateObjectWithFactory {
                val obj = StateObjectWithFactory(nameThing, age, primaryAddress, wrongName, createdDt)
                obj.factoryUsed = true
                return obj
            }
        }
    }

    @Test fun findingFactoryMethod() {
        val stateObj = normalCasedMapper.readValue(normalCasedJson, StateObjectWithFactory::class.java)
        stateObj.validate()
        val newStateObj = StateObjectWithFactory.create(stateObj.name, stateObj.age, stateObj.primaryAddress,
            stateObj.wrongName, stateObj.createdDt)
        assertEquals(true, newStateObj.factoryUsed, "Factory method was not used")
    }

    private class StateObjectWithFactoryNoParamAnnotations(
        val name: String,
        val age: Int,
        val primaryAddress: String,
        val renamed: Boolean,
        val createdDt: Date
    ) {
        companion object {
            @JvmStatic @JsonCreator fun create(
                name: String,
                age: Int,
                primaryAddress: String,
                renamed: Boolean,
                createdDt: Date
            ): StateObjectWithFactoryNoParamAnnotations {
                return StateObjectWithFactoryNoParamAnnotations(name, age, primaryAddress, renamed, createdDt)
            }
        }
    }

    @Test fun findingFactoryMethod2() {
        try {
            normalCasedMapper.readValue(normalCasedJson, StateObjectWithFactoryNoParamAnnotations::class.java)
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            fail("Exception not expected")
        }
    }

    private class StateObjectWithFactoryOnNamedCompanion private constructor(
        override val name: String,
        override val age: Int,
        override val primaryAddress: String,
        override val wrongName: Boolean,
        override val createdDt: Date
    ) : TestFields {
        var factoryUsed: Boolean = false
        companion object Named {
            @JvmStatic @JsonCreator fun create(
                @JsonProperty("name") nameThing: String,
                @JsonProperty("age") age: Int,
                @JsonProperty("primaryAddress") primaryAddress: String,
                @JsonProperty("renamed") wrongName: Boolean,
                @JsonProperty("createdDt") createdDt: Date
            ): StateObjectWithFactoryOnNamedCompanion {
                val obj = StateObjectWithFactoryOnNamedCompanion(nameThing, age, primaryAddress, wrongName, createdDt)
                obj.factoryUsed = true
                return obj
            }
        }
    }

    @Test fun findingFactoryMethod3() {
        val stateObj = normalCasedMapper.readValue(normalCasedJson, StateObjectWithFactoryOnNamedCompanion::class.java)
        stateObj.validate()
        val newStateObj = StateObjectWithFactoryOnNamedCompanion.create(stateObj.name, stateObj.age, stateObj.primaryAddress,
            stateObj.wrongName, stateObj.createdDt)
        assertEquals(true, newStateObj.factoryUsed, "Factory method was not used")
    }

    // GH #14 failing due to this enum type
    data class Gh14FailureWithEnum(var something: String = "hi", var someEnum: LaunchType = LaunchType.ACTIVITY)

    enum class LaunchType {
        ACTIVITY
    }

    @Test fun testGithub14() {
        val json = normalCasedMapper.writeValueAsString(Gh14FailureWithEnum())
        assertEquals("""{"something":"hi","someEnum":"ACTIVITY"}""", json)
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Test fun testFindKotlinClass() {
        assertFalse(HashMap::class.java.isKotlinClass())
        assertFalse(Object::class.java.isKotlinClass())
        assertFalse(String::class.java.isKotlinClass())
        assertFalse(ArrayList::class.java.isKotlinClass())
        assertTrue(DefaultAndSpecificConstructor::class.java.isKotlinClass())
        assertTrue(NoFailWithoutJsonCreator::class.java.isKotlinClass())
        assertTrue(StateObjectAsDataClassExplicitJsonCreator::class.java.isKotlinClass())
        assertTrue(StateObjectAsDataClassWithJsonCreatorAndJsonProperty::class.java.isKotlinClass())
        assertTrue(StateObjectAsNormalClass::class.java.isKotlinClass())
        assertTrue(StateObjectWithPartialFieldsInConstructor::class.java.isKotlinClass())
        assertTrue(StateObjectAsDataClassConfusingConstructor::class.java.isKotlinClass())
        assertTrue(StateObjectWithFactory::class.java.isKotlinClass())
        assertTrue(StateObjectWithFactoryNoParamAnnotations::class.java.isKotlinClass())
        assertTrue(Gh14FailureWithEnum::class.java.isKotlinClass())
    }
}
