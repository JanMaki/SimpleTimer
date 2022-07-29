package dev.simpletimer.data.lang.lang_data.command_info

import kotlinx.serialization.Serializable

/**
 * コマンドとの情報のデータ
 *
 * @property name
 * @property description
 * @constructor Create empty Command info lang data
 */
@Serializable
data class CommandInfo(val name: String = "", val description: String = "")