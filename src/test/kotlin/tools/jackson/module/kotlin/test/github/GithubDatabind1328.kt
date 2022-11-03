package tools.jackson.module.kotlin.test.github

import tools.jackson.annotation.JsonSubTypes
import tools.jackson.annotation.JsonTypeInfo
import tools.jackson.annotation.JsonTypeName
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithubDatabind1328 {
    @Test
    fun testPolymorphicWithEnum() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()
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