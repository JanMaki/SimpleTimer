package dev.simpletimer.data.config

import dev.simpletimer.data.serializer.ConfigValueSerializer
import kotlinx.serialization.Serializable

/**
 * コンフィグのデータ
 *
 * @property token BotのToken
 * @property loggingChannels ログを送信するチャンネル
 */
@Serializable
data class ConfigData(
    val token: @Serializable(with = ConfigValueSerializer::class) ConfigValue = ConfigValue("\$SIMPLETIMER_TOKEN"),
    val apiURL: @Serializable(with = ConfigValueSerializer::class) ConfigValue = ConfigValue("\$SIMPLETIMER_API_URL"),
    val apiToken: @Serializable(with = ConfigValueSerializer::class) ConfigValue =  ConfigValue(),
    val shardsCount: @Serializable(with = ConfigValueSerializer::class) ConfigValue =  ConfigValue("\$SIMPLETIMER_SHARDS_COUNT"),
    val loggingChannels: List<@Serializable(with = ConfigValueSerializer::class) ConfigValue> = mutableListOf(),
    val databaseConfig: DatabaseConfig = DatabaseConfig()
)