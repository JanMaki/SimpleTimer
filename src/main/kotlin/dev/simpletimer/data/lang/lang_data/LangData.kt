package dev.simpletimer.data.lang.lang_data

import dev.simpletimer.data.lang.lang_data.command.CommandLangData
import kotlinx.serialization.Serializable

/**
 * 言語のデータ
 *
 * @property timer タイマーの言語のデータ
 * @property command コマンドの言語のデータ
 */
@Serializable
data class LangData(
    val timer: TimerLangData = TimerLangData(),
    val command: CommandLangData = CommandLangData()
)
