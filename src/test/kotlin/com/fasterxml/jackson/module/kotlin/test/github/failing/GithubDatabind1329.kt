package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Broken in databind 2.8.0+ (not 2.8.0.rc2 which works) and not a problem with the Kotlin module
 */
class GithubDatabind1329 {
    @Test
    fun testPolymorphicWithEnum() {
        val mapper = jacksonObjectMapper()
        val invite = mapper.readValue<Invite>(
                """|{
                   |  "kind": "CONTACT",
                   |  "kindForMapper": "CONTACT",
                   |  "to": {
                   |    "name": "Foo"
                   |  }
                   |}""".trimMargin()
        )

        assertEquals(InviteKind.CONTACT, invite.kind)
        expectFailure<AssertionError>("GitHub Databind issue #1329 has been fixed!") {
            assertNull(invite.kindForMapper)
        }

        assertEquals("Foo", (invite.to as InviteToContact).name)
    }

    data class Invite(
            val kind: InviteKind,
            // workaround for https://github.com/FasterXML/jackson-databind/issues/999 (should be fixed in 2.8.x)
            val kindForMapper: String? = null,
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "kindForMapper", visible = false)
            @JsonSubTypes(
                    JsonSubTypes.Type(InviteToContact::class),
                    JsonSubTypes.Type(InviteToUser::class)
            )
            val to: InviteTo
    )

    interface InviteTo

    @JsonTypeName("CONTACT")
    data class InviteToContact(
            val name: String? = null
    ) : InviteTo

    @JsonTypeName("USER")
    data class InviteToUser(
            val user: String
    ) : InviteTo

    enum class InviteKind {
        CONTACT,
        USER
    }
}
