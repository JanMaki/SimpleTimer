package dev.simpletimer.data.config

import kotlinx.serialization.Serializable

/**
 * コンフィグで使用する値
 *
 * @property envKey 環境変数
 * @property value 実際の値
 */
@Serializable
data class ConfigValue(val envKey: String? = null, val value: String = "")