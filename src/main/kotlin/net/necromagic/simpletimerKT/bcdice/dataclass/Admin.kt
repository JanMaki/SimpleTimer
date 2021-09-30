package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [/v2/admin](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#admin)
 *
 * @property name [String] 管理者の名前
 * @property url [String] 利用規約等が書かれたページのURL
 * @property email [String] 連絡先メールアドレス
 */
@Serializable
data class Admin(
    @SerialName("name")
    val name: String,

    @SerialName("url")
    val url: String,

    @SerialName("email")
    val email: String
)
