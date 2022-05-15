package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * キューコマンドの言語のデータ
 *
 * @property queueNotFount
 * @property remove 0->number
 * @property clear 0->number
 */
@Serializable
data class QueueCommandLangData(
    val queueNotFount: String = "",
    val remove: String = "",
    val clear: String = ""
)