package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [/v2/game_system](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#game-system)
 *
 * @property id [String] ゲームシステムのID
 * @property name [String] ゲームシステムの名前
 * @property sort_key [String] ゲームシステムをソートするためのキー
 * @constructor Create empty Game system
 */
@Serializable
data class GameSystem(
    var page: Int = -1,
    var number: Int = -1,

    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("sort_key")
    val sort_key: String

)
