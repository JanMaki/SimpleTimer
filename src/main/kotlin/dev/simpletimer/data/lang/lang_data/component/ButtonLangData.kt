package dev.simpletimer.data.lang.lang_data.component

import kotlinx.serialization.Serializable

@Serializable
data class ButtonLangData(
    val addTimer: String = "",
    val finishTimer: String = "",
    val restartTimer: String = "",
    val stopTimer: String = "",
    val startTime: String = "",
    val timerMaxWarning: String = "",
    val joinCommunity: String = "",
    val roll: String = ""
)