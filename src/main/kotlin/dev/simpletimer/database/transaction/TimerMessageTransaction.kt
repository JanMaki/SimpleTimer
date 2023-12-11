package dev.simpletimer.database.transaction

import dev.simpletimer.database.Connector
import dev.simpletimer.database.data.TimerData
import dev.simpletimer.database.data.TimerMessageData
import dev.simpletimer.database.table.TimerMessageTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

/**
 * [TimerMessageTable]のトランザクション
 */
object TimerMessageTransaction {
    /**
     * [TimerMessageData]を更新・挿入する
     *
     * @param timerDataId 対象の[TimerData]のID
     * @param timerMessageData [TimerMessageData]
     */
    fun upsertTimerMessageData(timerDataId: Long, timerMessageData: TimerMessageData) {
        Connector.connect()

        //UPSERT
        transaction {
            TimerMessageTable.upsert {
                it[TimerMessageTable.timerDataId] = timerDataId
                it[messageId] = timerMessageData.messageId
                it[messageType] = timerMessageData.messageType
            }
        }
    }

    /**
     * メッセージがDisplayのメッセージかを確認
     *
     * @param messageId 対象のメッセージのId
     * @return Displayのメッセージの場合はtrueを返す
     */
    fun isDisplayMessage(messageId: Long): Boolean {
        Connector.connect()

        //SELECTした結果がnullじゃない場合はtrue
        return transaction {
            TimerMessageTable.select {
                TimerMessageTable.messageId.eq(messageId)
                    .and(TimerMessageTable.messageType.eq(TimerMessageData.MessageType.DISPLAY))
            }.firstOrNull()
        } != null
    }

    /**
     * メッセージから[TimerData]を取得する
     *
     * @param messageId 対象のメッセージのId
     * @return [TimerData]?
     */
    fun getTimerDataByMessage(messageId: Long): TimerData? {
        Connector.connect()

        //SELECT
        val timerDataId = transaction {
            TimerMessageTable.select {
                TimerMessageTable.messageId eq messageId
            }.firstOrNull()?.let {
                //TimerMessageDataにあるTimerDataのIdを取得
                return@transaction it[TimerMessageTable.timerDataId]
            }
        } ?: return null

        //タイマーのデータを取得して返す
        return TimerDataTransaction.getTimerData(timerDataId)
    }

    /**
     * [TimerData]から紐づいている[TimerMessageData]を取得
     *
     * @param timerData [TimerData]
     * @return [List]<[TimerData]>
     */
    fun getTimerMessagesFromTimerData(timerData: TimerData): List<TimerMessageData> {
        Connector.connect()

        //SELECT
        return transaction {
            TimerMessageTable.select {
                TimerMessageTable.timerDataId eq timerData.timerDataId
            }.map {
                TimerMessageData(
                    it[TimerMessageTable.messageId],
                    it[TimerMessageTable.messageType]
                )
            }
        }
    }

    /**
     * メッセージのIdに該当するレコードを削除する
     *
     * @param messageId メッセージのId
     */
    fun removeTimerMessageData(messageId: Long) {
        Connector.connect()

        transaction {
            TimerMessageTable.deleteWhere { TimerMessageTable.messageId.eq(messageId) }
        }
    }
}