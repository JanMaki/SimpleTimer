package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * Chromeの拡張機能コマンドのデータ
 *
 * @property output 0->number
 */
@Serializable
data class ChromeCommandLangData(
    val output: String = ""
)