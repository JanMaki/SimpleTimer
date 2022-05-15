package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * ヘルプコマンドの言語のデータ
 *
 * @property explanation
 * @property whatIsSimpleTimer
 * @property timerCommandsList
 * @property listExplanation
 * @property developInfo
 * @property sourceCode
 * @property supportForDeveloper
 * @property show
 * @property termsOfUse
 * @property policy
 * @constructor Create empty Help command lang data
 */
@Serializable
data class HelpCommandLangData(
    val explanation: String = "",
    val whatIsSimpleTimer: String = "",
    val timerCommandsList: String = "",
    val listExplanation: String = "",
    val developInfo: String = "",
    val sourceCode: String = "",
    val supportForDeveloper: String = "",
    val show: String = "",
    val termsOfUse: String = "",
    val policy: String = ""

)