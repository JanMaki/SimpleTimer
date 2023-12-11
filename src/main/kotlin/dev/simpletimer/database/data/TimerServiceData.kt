package dev.simpletimer.database.data

import kotlinx.serialization.Serializable

/**
 * タイマーの状態
 *
 * @property isStarted 始まっているか
 * @property isMove 動いているか
 * @property isFinish 終了しているか
 * @property startNanoTime タイマーが開始した時間
 * @property adjustTime タイマーの調整値
 * @property stopTime 一時停止した時の時間を保管 調整に使用
 */
@Serializable
data class TimerServiceData(
    var isStarted: Boolean = false,
    var isMove: Boolean = true,
    var isFinish: Boolean = false,
    var startNanoTime: Long = 0L,
    var adjustTime: Long = 0L,
    var stopTime: Long = 0L
)