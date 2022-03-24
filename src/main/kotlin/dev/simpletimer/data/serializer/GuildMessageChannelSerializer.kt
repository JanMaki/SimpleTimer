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
import net.dv8tion.jda.api.entities.GuildMessageChannel

/**
 * [GuildMessageChannel]のSerializer
 *
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = GuildMessageChannel::class)
object GuildMessageChannelSerializer : KSerializer<GuildMessageChannel?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GuildMessageChannel", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: GuildMessageChannel?) {
        //idを取得
        val idLong = value?.idLong ?: return
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): GuildMessageChannel? {
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からTextChannelとThreadChannelを取得
            return jda.getTextChannelById(decoder.decodeLong()) ?: jda.getThreadChannelById(decoder.decodeLong())
            ?: return@forEach
        }
        return null
    }
}