package dev.simpletimer.data.lang.lang_data

import kotlinx.serialization.Serializable

/**
 * タイマーの言語のデータ
 *
 * @property minutes 0 -> number
 * @property seconds 0 -> number
 * @property start 0 -> time
 * @property minutesLeftNotice 0 -> minutes
 * @property add 0 -> minutes&seconds
 * @property minus 0 -> minutes&seconds
 * @property notStop
 * @property restart 0 -> time
 * @property restartTTS
 * @property alreadyStop
 * @property stop
 * @property finish
 * @property leftCheck 0 -> time
 * @property remove
 * @property timerMaxWarning
 * @property queueNumber 0 -> number
 */
@Serializable
data class TimerLangData(
    val minutes: String = "",
    val seconds: String = "",
    val start: String = "",
    val minutesLeftNotice: String = "",
    val add: String = "",
    val minus: String = "",
    val notStop: String = "",
    val restart: String = "",
    val restartTTS: String = "",
    val alreadyStop: String = "",
    val stop: String = "",
    val finish: String = "",
    val leftCheck: String = "",
    val remove: String = "",
    val timerMaxWarning: String = "",
    val queueNumber: String = ""
)