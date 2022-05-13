package dev.simpletimer.data.lang.lang_data

import dev.simpletimer.data.lang.lang_data.command.CommandLangData
import dev.simpletimer.data.lang.lang_data.component.ComponentLangData
import kotlinx.serialization.Serializable

/**
 * 言語のデータ
 *
 * @property timer タイマーの言語のデータ
 * @property command コマンドの言語のデータ
 * @property component コンポーネントの言語のデータ
 */
@Serializable
data class LangData(
    val timer: TimerLangData = TimerLangData(),
    val command: CommandLangData = CommandLangData(),
    val component: ComponentLangData = ComponentLangData(),
)
