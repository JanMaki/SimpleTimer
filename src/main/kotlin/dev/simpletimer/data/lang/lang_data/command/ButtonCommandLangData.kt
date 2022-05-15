package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * ボタンコマンドの言語のデータ
 *
 * @property roll 0->text
 */
@Serializable
data class ButtonCommandLangData(
    val roll: String = ""
)