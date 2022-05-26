package dev.simpletimer.data.lang.lang_data.dice

import kotlinx.serialization.Serializable

/**
 * BCDiceの言語のデータ
 *
 * @property botSelect
 * @property botSelectDescription
 * @property page 0,1 -> number
 * @property changeDiceBot 0,1 -> text
 * @property diceDescription
 * @property gameCommand
 * @property commonCommand
 */
@Serializable
class BCDiceLangData(
    val botSelect: String = "",
    val botSelectDescription: String = "",
    val page: String = "",
    val changeDiceBot: String = "",
    val diceDescription: String = "",
    val gameCommand: String = "",
    val commonCommand: String = "",
    val unknownBot: String = ""
)