package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [/v2/game_system](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#game-system)
 *
 * @property game_system [Array] GameSystemの一覧[GameSystem]
 */
@Serializable
data class GameSystemArray(
    @SerialName("game_system")
    val game_system: Array<GameSystem>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameSystemArray

        if (!game_system.contentEquals(other.game_system)) return false

        return true
    }

    override fun hashCode(): Int {
        return game_system.contentHashCode()
    }
}