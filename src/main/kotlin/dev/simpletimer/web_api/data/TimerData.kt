package dev.simpletimer.web_api.data

import kotlinx.serialization.Serializable

/**
 * タイマーのデータ
 *
 * @property seconds 残り時間
 * @property timeStamp 更新時間
 * @property isMove　タイマーが動いているか
 * @property isFinish　タイマーが終了しているか
 */
@Serializable
data class TimerData(
    val seconds: Int = 0,
    val timeStamp: Long = 0L,
    val isMove: Boolean = false,
    val isFinish: Boolean = false
)
