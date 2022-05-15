package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * ダイスコマンドの言語のデータ
 *
 * @property selectInMenu
 */
@Serializable
data class DiceCommandLangData(
    val selectInMenu: String = ""
)