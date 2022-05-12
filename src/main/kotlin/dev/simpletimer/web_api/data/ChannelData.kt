package dev.simpletimer.web_api.data

import kotlinx.serialization.Serializable


/**
 * チャンネルのデータ
 *
 * @property channel チャンネルのID
 * @property timers タイマーの一覧
 */
@Serializable
data class ChannelData(
    val channel: Long,
    val timers: List<TimerData>
)