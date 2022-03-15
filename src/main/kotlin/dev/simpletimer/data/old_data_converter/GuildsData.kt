package dev.simpletimer.data.old_data_converter

import dev.simpletimer.data.guild.GuildData
import kotlinx.serialization.Serializable

/**
 * ギルドのデータのマップ
 *
 * @property guilds ギルドのIDとデータのマップ
 */
@Deprecated("旧仕様のため使用を廃止")
@Serializable
data class GuildsData(
    val guilds: MutableMap<Long, GuildData>
)