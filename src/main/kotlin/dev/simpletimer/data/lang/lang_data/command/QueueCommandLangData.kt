package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

@Serializable
class QueueCommandLangData(
    val queueNotFount: String = "",
    val remove: String = "",
    val clear: String = ""
)