package dev.simpletimer.data.enum

/**
 * TTSのタイミングの列挙
 * [LV0] TTSによる通知を行わない
 * [LV1] 終了時のみ送信
 * [LV2] 終了時と、定期的な時間通知の時に実行
 * [LV3] タイマーのすべての通知（上に加えて、延長など）で実行
 */
enum class TTSTiming(var priority: Int) {
    LV0(0),
    LV1(1),
    LV2(2),
    LV3(3)
}