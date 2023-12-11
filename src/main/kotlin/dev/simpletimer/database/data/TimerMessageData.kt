package dev.simpletimer.database.data

import kotlinx.serialization.Serializable


/**
 * タイマーのメッセージのデータ
 *
 * @property message メッセージのID
 * @property messageType メッセージの種類
 */
@Serializable
data class TimerMessageData(
    val messageId: Long,
    val messageType: MessageType
) {
    /**
     * メッセージの種類
     *
     */
    enum class MessageType {
        //通常のメッセージ
        NORMAL,

        //タイマー表示用メッセージ
        DISPLAY
    }
}