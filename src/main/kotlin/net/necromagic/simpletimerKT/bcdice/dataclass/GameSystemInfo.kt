package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [/v2/game_system](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#game-system-info)
 *
 * @property id [String] ゲームシステムのID
 * @property name [String] ゲームシステムの名前
 * @property sort_key [String] ゲームシステムをソートするためのキー
 * @property command_pattern [String] 実行可能なコマンドか判定するための正規表現。これにマッチするテキストがコマンドとして実行できる可能性がある。利用する際には大文字か小文字かを無視すること
 * @property help_message [String] ヘルプメッセージ
 */
@Serializable
data class GameSystemInfo(
    @SerialName("ok")
    val ok: Boolean,

    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("sort_key")
    val sort_key: String,

    @SerialName("command_pattern")
    val command_pattern: String,

    @SerialName("help_message")
    val help_message: String
)