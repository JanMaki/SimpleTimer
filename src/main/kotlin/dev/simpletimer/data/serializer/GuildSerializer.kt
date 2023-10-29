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
import net.dv8tion.jda.api.entities.Guild

/**
 * [Guild]のSerializer
 *
 */
object GuildSerializer : KSerializer<Guild?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Guild", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Guild?) {
        //idを取得
        val idLong = value?.idLong ?: return
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): Guild? {
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からGuildを取得
            return jda.getGuildById(decoder.decodeLong()) ?: return@forEach
        }
        return null
    }
}