package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * 言語変更コマンドの言語のデータ
 *
 * @property change 0->text
 */
@Serializable
data class LangCommandLangData(
    val change: String = ""
)