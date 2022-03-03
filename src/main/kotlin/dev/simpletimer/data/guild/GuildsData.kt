package dev.simpletimer.data.guild

import kotlinx.serialization.Serializable

/**
 * ギルドのデータのマップ
 *
 * @property guilds ギルドのIDとデータのマップ
 */
@Serializable
data class GuildsData(
    val guilds: MutableMap<Long, GuildData>
)