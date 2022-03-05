package dev.simpletimer.data.enum

/**
 * 通知タイミングの列挙
 * [LV0] 使わない
 * [LV1] 終了時
 * [LV2] 終了時と、定期的な時間通知
 * [LV3] タイマーのすべての通知
 * [NONE] 送信しない
 */
enum class NoticeTiming(var priority: Int) {
    LV0(0),
    LV1(1),
    LV2(2),
    LV3(3),
    NONE(999)
}