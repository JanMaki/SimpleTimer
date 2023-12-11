package dev.simpletimer.data.serializer

import dev.simpletimer.SimpleTimer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.dv8tion.jda.api.entities.Role

/**
 * [Role]のSerializer
 *
 */
object RoleSerializer : KSerializer<Role?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Role", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Role?) {
        //idを取得
        val idLong = value?.idLong ?: return
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): Role? {
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からRoleを取得
            return jda.getRoleById(decoder.decodeLong()) ?: return@forEach
        }
        return null
    }
}