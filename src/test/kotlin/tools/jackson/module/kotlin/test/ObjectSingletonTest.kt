package tools.jackson.module.kotlin.test

import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.KotlinFeature.SingletonSupport
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

// [module-kotlin#225]: keep Kotlin singletons as singletons
class TestObjectSingleton {
    val mapper: ObjectMapper = ObjectMapper()
        .registerModule(_root_ide_package_.tools.jackson.module.kotlin.kotlinModule { enable(SingletonSupport) })

    object Singleton {
        var content = 1 // mutable state
    }

    @Test
    fun deserializationPreservesSingletonProperty() {
        val js = mapper.writeValueAsString(Singleton)
        val newSingleton = mapper.readValue<Singleton>(js)

        assertThat(newSingleton, equalTo(Singleton))
    }

    @Test
    fun deserializationResetsSingletonObjectState() {
        // persist current singleton state
        val js = mapper.writeValueAsString(Singleton)
        val initial = Singleton.content

        // mutate the in-memory singleton state
        val after = initial + 1
        Singleton.content = after
        assertThat(Singleton.content, equalTo(after))

        // read back persisted state resets singleton state
        val newSingleton = mapper.readValue<Singleton>(js)
        assertThat(newSingleton.content, equalTo(initial))
        assertThat(Singleton.content, equalTo(initial))
    }

    @Test
    fun deserializedObjectsBehaveLikeSingletons() {
        val js = mapper.writeValueAsString(Singleton)
        val newSingleton = mapper.readValue<Singleton>(js)
        assertThat(newSingleton.content, equalTo(Singleton.content))

        newSingleton.content += 1

        assertThat(Singleton.content, equalTo(newSingleton.content))
    }
}
