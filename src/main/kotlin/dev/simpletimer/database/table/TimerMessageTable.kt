package dev.simpletimer.database.table

import dev.simpletimer.database.data.TimerMessageData
import org.jetbrains.exposed.sql.Table


/**
 * タイマーのメッセージの[Table]
 *
 */
object TimerMessageTable : Table("timer_message") {
    //TimerDataにて使用しているID 外部キー
    val timerDataId = long("timer_data_id").references(TimerDataTable.timerDataId)

    //メッセージの種類
    val messageType = enumeration<TimerMessageData.MessageType>("message_type")

    //メッセージのID
    val messageId = long("message_id")

    override val primaryKey = PrimaryKey(timerDataId, messageType)
}