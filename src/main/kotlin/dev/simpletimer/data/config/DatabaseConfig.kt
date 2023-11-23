package dev.simpletimer.data.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val scheme: String = "",
    val user: String = "",
    val password: String = ""
)