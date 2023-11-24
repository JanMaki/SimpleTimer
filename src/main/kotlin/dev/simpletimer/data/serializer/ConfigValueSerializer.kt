package dev.simpletimer.data.serializer

import dev.simpletimer.data.config.ConfigValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [ConfigValue]の[KSerializer]
 *
 */
@Suppress("UNREACHABLE_CODE")
class ConfigValueSerializer : KSerializer<ConfigValue> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ConfigValue", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): ConfigValue {
        val value = decoder.decodeString()
        return if (value.startsWith("$")) {
            return ConfigValue(value, System.getenv(value.replaceFirst("$", "")) ?: "")
        } else {
            return ConfigValue(value = value)
        }
    }

    override fun serialize(encoder: Encoder, value: ConfigValue) {
        return if (value.envKey != null) {
            encoder.encodeString(value.envKey)
        } else {
            encoder.encodeString(value.value)
        }
    }
}