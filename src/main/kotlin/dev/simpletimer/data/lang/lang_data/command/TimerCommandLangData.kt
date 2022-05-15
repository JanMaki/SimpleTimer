package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * タイマーコマンドの言語用データ
 *
 * @property ttsTiming 0->text
 * @property maxMessageLengthWarning
 * @property finishTTS
 * @property mentionTiming 0->text
 * @property mentionSetting 0->text
 * @property targetRole 0->text
 * @property targetRoleEmpty
 * @property targetRolePrompt
 * @property targetVC 0->text
 * @property targetVCEmpty
 * @property targetVCPrompt
 * @property addRole 0->text
 * @property removeRole 0->text
 * @property notVCWaring
 * @property addVC 0->text
 * @property removeVC  0->text
 * @constructor Create empty Timer command lang data
 */
@Serializable
data class TimerCommandLangData(
    val ttsTiming: String = "",
    val maxMessageLengthWarning: String = "",
    val finishTTS: String = "",
    val mentionTiming: String = "",
    val mentionSetting: String = "",
    val targetRole: String = "",
    val targetRoleEmpty: String = "",
    val targetRolePrompt: String = "",
    val targetVC: String = "",
    val targetVCEmpty: String = "",
    val targetVCPrompt: String = "",
    val addRole: String = "",
    val removeRole: String = "",
    val notVCWaring: String = "",
    val addVC: String = "",
    val removeVC: String = ""
)