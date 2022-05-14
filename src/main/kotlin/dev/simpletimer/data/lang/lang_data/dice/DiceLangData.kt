package dev.simpletimer.data.lang.lang_data.dice

import kotlinx.serialization.Serializable

/**
 * ダイスの言語のデータ
 *
 * @property secret
 * @property longRangeWaring 0->mention
 * @property wrongFormat 0->mention
 * @property changeDiceMode 0->text
 * @property defaultDice SimpleTimer標準ダイスの言語のデータ
 * @property bcDice BCDIceのLangのデータ
 */
@Serializable
data class DiceLangData(
    val secret: String = "",
    val longRangeWaring: String = "",
    val wrongFormat: String = "",
    val changeDiceMode: String = "",
    val defaultDice: DefaultDiceLangData = DefaultDiceLangData(),
    val bcDice: BCDiceLangData = BCDiceLangData()
)