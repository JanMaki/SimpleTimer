package dev.simpletimer.data.lang.lang_data.dice

import kotlinx.serialization.Serializable

/**
 * SimpleTimer標準ダイスの言語のデータ
 *
 * @property title
 * @property dDescription
 * @property plusDescription
 * @property lessThanDescription
 * @property lessDescription
 * @property omission
 * @property success
 * @property failure
 */
@Serializable
data class DefaultDiceLangData(
    val title: String = "",
    val dDescription: String = "",
    val plusDescription: String = "",
    val lessThanDescription: String = "",
    val lessDescription: String = "",
    val omission: String = "",
    val success: String = "",
    val failure: String = ""

)