package dev.simpletimer.data.serializer

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.guild.GuildData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.dv8tion.jda.api.entities.MessageChannel

/**
 * [MessageChannel]のSerializer
 *
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = MessageChannel::class)
object MessageChannelSerializer : KSerializer<MessageChannel?> {
    override val descriptor: SerialDescriptor = GuildData.serializer().descriptor

    override fun serialize(encoder: Encoder, value: MessageChannel?) {
        //idを取得
        val idLong = value?.idLong ?: 0
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): MessageChannel? {
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からTextChannelとThreadChannelを取得
            return jda.getTextChannelById(decoder.decodeLong()) ?: jda.getThreadChannelById(decoder.decodeLong())
            ?: return@forEach
        }
        return null
    }
}