package dev.simpletimer.data.config

import kotlinx.serialization.Serializable

/**
 * コンフィグのデータ
 *
 * @property token BotのToken
 */
@Serializable
data class ConfigData(
    val token: String = "TOKEN IS HERE",
    val loggingChannels: List<Long> = mutableListOf()
)