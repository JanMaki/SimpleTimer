package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [/v2/game_system/{id}/roll](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#dice-roll)
 *
 * @property text [String] コマンドの出力
 * @property secret [Boolean] シークレットダイスか
 * @property success [Boolean] 結果が成功か
 * @property failure [Boolean] 結果が失敗か
 * @property critical [Boolean] 結果がクリティカルか
 * @property fumble [Boolean] 結果がファンブルか
 * @property rands [Array] ダイス目の詳細[Rands]
 * @constructor Create empty Dice roll
 */
@Serializable
data class DiceRoll(
    @SerialName("ok")
    val ok: Boolean,

    @SerialName("text")
    val text: String,

    @SerialName("secret")
    val secret: Boolean,

    @SerialName("success")
    val success: Boolean,

    @SerialName("failure")
    val failure: Boolean,

    @SerialName("critical")
    val critical: Boolean,

    @SerialName("fumble")
    val fumble: Boolean,

    @SerialName("rands")
    val rands: Array<Rands>
) {

    /**
     * Equalsの実装
     *
     * @param other [Any] 比較対象
     * @return [Boolean] 比較の結果
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiceRoll

        if (text != other.text) return false
        if (secret != other.secret) return false
        if (success != other.success) return false
        if (failure != other.failure) return false
        if (critical != other.critical) return false
        if (fumble != other.fumble) return false
        if (!rands.contentEquals(other.rands)) return false

        return true
    }

    /**
     * HashCodeの生成の実装
     *
     * @return [Int] 生成結果
     */
    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + success.hashCode()
        result = 31 * result + failure.hashCode()
        result = 31 * result + critical.hashCode()
        result = 31 * result + fumble.hashCode()
        result = 31 * result + rands.contentHashCode()
        return result
    }
}
