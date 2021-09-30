package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Rands](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#rands)
 *
 * @property kind [String] ダイスロールの種類。'nomal', 'tens_d10', 'd9'の3種類
 * @property sides [Int] ダイスロールしたダイスの面数
 * @property value [Int] 出目の数
 */
@Serializable
data class Rands(
    @SerialName("kind")
    val kind: String,

    @SerialName("sides")
    val sides: Int,

    @SerialName("value")
    val value: Int
)
