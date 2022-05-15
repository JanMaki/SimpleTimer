package dev.simpletimer.data.lang.lang_data.component

import kotlinx.serialization.Serializable

/**
 * SelectMenuの言語のデータ
 *
 * @property placeholder
 * @property runListEntry 0->text
 * @constructor Create empty Select menu lang data
 */
@Serializable
data class SelectMenuLangData(
    val placeholder: String = "",
    val runListEntry: String = ""
)