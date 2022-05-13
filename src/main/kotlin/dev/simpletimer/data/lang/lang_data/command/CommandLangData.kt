package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * コマンド関係の言語のデータ
 *
 * @property audio オーディオコマンドの言語のデータ
 * @constructor Create empty Command lang data
 */
@Serializable
data class CommandLangData(
    val audio: AudioCommandLangData = AudioCommandLangData()
)