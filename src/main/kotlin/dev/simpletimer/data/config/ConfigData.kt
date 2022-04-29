package dev.simpletimer.data.config

import kotlinx.serialization.Serializable

/**
 * コンフィグのデータ
 *
 * @property token BotのToken
 * @property loggingChannels ログを送信するチャンネル
 */
@Serializable
data class ConfigData(
    val token: String = "TOKEN IS HERE",
    val apiURL: String = "http://localhost:8080",
    val apiToken: String = "",
    val loggingChannels: List<Long> = mutableListOf()
)