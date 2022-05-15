package dev.simpletimer.data.lang.lang_data.component

import kotlinx.serialization.Serializable

/**
 * Modalの言語のデータ
 *
 * @property minutes
 * @property seconds
 * @property missingTimeWarning
 * @property timerNotMoveWarning
 * @property addTimeMinutes
 * @property addTimeSeconds
 * @property addTime
 * @property moreThanOneWarning
 * @property addQueue 0->number
 * @property startTimer
 * @property timerButton
 * @property createButton 0-minutes&seconds
 * @property missingValue
 * @property yesNoWarning
 * @property check
 * @property yesNo
 * @property confirmation
 */
@Serializable
data class ModalLangData(
    val minutes: String = "",
    val seconds: String = "",
    val missingTimeWarning: String = "",
    val addTimeMinutes: String = "",
    val addTimeSeconds: String = "",
    val addTime: String = "",
    val moreThanOneWarning: String = "",
    val addQueue: String = "",
    val startTimer: String = "",
    val timerButton: String = "",
    val createButton: String = "",
    val missingValue: String = "",
    val yesNoWarning: String = "",
    val check: String = "",
    val yesNo: String = "",
    val confirmation: String = ""
)