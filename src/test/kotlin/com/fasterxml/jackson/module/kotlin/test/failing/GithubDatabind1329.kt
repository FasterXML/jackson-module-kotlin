package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class GithubDatabind1329 {
    @Test
// Under `failing`, no need to ignore (but can run from IDE more easily)
//    @Ignore("Broken in databind 2.8.0+ (not 2.8.0.rc2 which works) and not a problem with the Kotlin module")
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
        assertNull(invite.kindForMapper)
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
    ): InviteTo

    @JsonTypeName("USER")
    data class InviteToUser(
            val user: String
    ): InviteTo

    enum class InviteKind {
        CONTACT,
        USER
    }
}