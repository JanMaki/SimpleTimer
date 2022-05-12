package dev.simpletimer.data.lang.lang_data

import kotlinx.serialization.Serializable

/**
 * コマンド関係の言語のデータ
 *
 * @property test
 * @constructor Create empty Command lang data
 */
@Serializable
data class CommandLangData(val test: String = "")