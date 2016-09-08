package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GithubDatabind1328 {
    @Test
    @Ignore("Appears to be a bug in databind and not Kotlin module, this is here for tracking")
    fun testPolymorphicWithEnum() {
        val mapper = jacksonObjectMapper()
        val invite = mapper.readValue<Invite>(
                """|{
                   |  "kind": "CONTACT",
                   |  "to": {
                   |    "name": "Foo"
                   |  }
                   |}""".trimMargin()
        )

        assertEquals(InviteKind.CONTACT, invite.kind)
        assertEquals("Foo", (invite.to as InviteToContact).name)
    }


    data class Invite(
            val kind: InviteKind,
            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "kind", visible = true)
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