package dev.simpletimer.data.config

import dev.simpletimer.data.serializer.ConfigValueSerializer
import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val address: @Serializable(with = ConfigValueSerializer::class) ConfigValue = ConfigValue("\$SIMPLETIMER_DB_ADDRESS"),
    val scheme: @Serializable(with = ConfigValueSerializer::class) ConfigValue = ConfigValue("\$SIMPLETIMER_DB_SCHEME"),
    val user: @Serializable(with = ConfigValueSerializer::class) ConfigValue = ConfigValue("\$SIMPLETIMER_DB_USER"),
    val password: @Serializable(with = ConfigValueSerializer::class) ConfigValue = ConfigValue("\$SIMPLETIMER_DB_PASS")
)