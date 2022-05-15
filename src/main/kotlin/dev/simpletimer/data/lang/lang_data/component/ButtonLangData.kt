package dev.simpletimer.data.lang.lang_data.component

import kotlinx.serialization.Serializable

/**
 * ボタンの言語のデータ
 *
 * @property addTimer
 * @property finishTimer
 * @property restartTimer
 * @property stopTimer
 * @property startTime
 * @property joinCommunity
 * @property roll
 */
@Serializable
data class ButtonLangData(
    val addTimer: String = "",
    val finishTimer: String = "",
    val restartTimer: String = "",
    val stopTimer: String = "",
    val startTime: String = "",
    val joinCommunity: String = "",
    val roll: String = ""
)