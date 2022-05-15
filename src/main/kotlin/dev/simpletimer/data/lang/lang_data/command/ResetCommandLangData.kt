package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * リセットコマンドの言語のデータ
 *
 * @property reset
 */
@Serializable
data class ResetCommandLangData(
    val reset: String = ""
)