package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * ボタンコマンドの言語のデータ
 *
 * @property longLengthWarning
 * @property roll 0->text
 */
@Serializable
data class ButtonCommandLangData(
    val longLengthWarning: String = "",
    val roll: String = ""
)