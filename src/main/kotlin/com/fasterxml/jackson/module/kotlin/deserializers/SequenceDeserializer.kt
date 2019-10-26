package com.fasterxml.jackson.module.kotlin.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * @author Konstantin Volivach
 */
class SequenceDeserializer : StdDeserializer<Sequence<*>>(Sequence::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Sequence<*> {
        return ctxt.readValue(p, List::class.java).asSequence()
    }
}