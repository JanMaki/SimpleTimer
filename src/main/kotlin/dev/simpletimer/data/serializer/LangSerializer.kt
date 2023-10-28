package dev.simpletimer.data.serializer

import dev.simpletimer.data.lang.Lang
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LangSerializer : KSerializer<Lang> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Lang", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Lang {
        val text = decoder.decodeString()
        Lang.entries.forEach {
            if (it.name == text) return it
        }
        return Lang.JPA
    }

    override fun serialize(encoder: Encoder, value: Lang) {
        encoder.encodeString(value.name)
    }
}