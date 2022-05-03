@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package dev.simpletimer.data.serializer

import dev.simpletimer.SimpleTimer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.dv8tion.jda.api.entities.AudioChannel


@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = AudioChannel::class)
object AudioChannelSerializer : KSerializer<AudioChannel?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AudioChannel", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: AudioChannel?) {
        //idを取得
        val idLong = value?.idLong ?: return
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): AudioChannel? {
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からAudioChannelを取得
            return jda.getVoiceChannelById(decoder.decodeLong()) ?: jda.getStageChannelById(decoder.decodeLong())
            ?: return@forEach
        }
        return null
    }
}