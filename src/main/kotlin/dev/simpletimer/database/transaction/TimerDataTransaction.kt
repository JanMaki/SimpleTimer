package dev.simpletimer.database.transaction

import dev.simpletimer.database.Connector
import dev.simpletimer.database.data.TimerData
import dev.simpletimer.database.data.TimerServiceData
import dev.simpletimer.database.table.TimerDataTable
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * [TimerDataTable]用のトランザクション
 */
object TimerDataTransaction {
    /**
     * [TimerData]を作成する
     *
     * @param channel [GuildMessageChannel]
     * @param number [Number]
     * @param seconds 秒数
     * @return 作成した[TimerData]
     */
    fun createTimerData(channel: GuildMessageChannel, number: Timer.Number, seconds: Int): TimerData {
        Connector.connect()

        //TimerDataを作成
        val timerData = TimerData(channel = channel, number = number, seconds = seconds)

        //INSERT
        transaction {
            TimerDataTable.insert {
                it[TimerDataTable.channel] = timerData.channel
                it[TimerDataTable.number] = timerData.number
                it[TimerDataTable.seconds] = timerData.seconds
                it[guildId] = timerData.channel.guild.idLong
                it[isStarted] = timerData.timerServiceData.isStarted
                it[isMove] = timerData.timerServiceData.isMove
                it[isFinish] = timerData.timerServiceData.isFinish
                it[startNanoTime] = timerData.timerServiceData.startNanoTime
                it[adjustTime] = timerData.timerServiceData.adjustTime
                it[stopTime] = timerData.timerServiceData.stopTime
            }.apply {
                //自動採番キーを取得
                timerData.timerDataId = this[TimerDataTable.timerDataId]
            }
        }

        return timerData
    }

    /**
     * [TimerDataTable]を更新する
     *
     * @param timerData [TimerData]
     */
    fun updateTimerData(timerData: TimerData) {
        Connector.connect()

        //UPDATE
        transaction {
            TimerDataTable.update({
                TimerDataTable.timerDataId eq timerData.timerDataId
            }) {
                it[channel] = timerData.channel
                it[number] = timerData.number
                it[seconds] = timerData.seconds
                it[displayMessageBase] = timerData.displayMessageBase
                it[guildId] = timerData.channel.guild.idLong
                it[isStarted] = timerData.timerServiceData.isStarted
                it[isMove] = timerData.timerServiceData.isMove
                it[isFinish] = timerData.timerServiceData.isFinish
                it[startNanoTime] = timerData.timerServiceData.startNanoTime
                it[adjustTime] = timerData.timerServiceData.adjustTime
                it[stopTime] = timerData.timerServiceData.stopTime
            }
        }
    }

    /**
     * [GuildMessageChannel]にある[TimerData]の一覧を取得
     *
     * @param channel 対象の[GuildMessageChannel]
     * @return [List]<[TimerData]>
     */
    fun getTimerData(channel: GuildMessageChannel): List<TimerData> {
        Connector.connect()

        //結果用変数
        val result: MutableList<TimerData> = mutableListOf()

        //SELECT
        transaction {
            TimerDataTable.select {
                //チャンネルと終了済みじゃないかを確認
                TimerDataTable.channel.eq(channel) and TimerDataTable.isFinish.eq(false)
            }.forEach {
                //結果用変数に追加
                result.add(
                    serializeTimerData(it)
                )
            }
        }

        return result
    }

    /**
     * [GuildMessageChannel]で動いている[Number]のタイマーを取得
     *
     * @param channel 対象の[GuildMessageChannel]
     * @param number 対象の[Number]
     * @return [TimerData]?
     */
    fun getTimerData(channel: GuildMessageChannel, number: Timer.Number): TimerData? {
        Connector.connect()

        //SELECT
        return transaction {
            TimerDataTable.select {
                //チャンネルと終了済みじゃないかとNumberを確認
                TimerDataTable.channel.eq(channel) and TimerDataTable.isFinish.eq(false) and TimerDataTable.number.eq(
                    number
                )
            }.firstOrNull()?.let {
                serializeTimerData(it)
            }
        }
    }

    /**
     * TimerDataの自動裁判Idから[TimerData]を取得
     *
     * @param timerDataId [TimerDataTable.timerDataId]
     * @return [TimerData]?
     */
    fun getTimerData(timerDataId: Long): TimerData? {
        Connector.connect()

        //SELECT
        return transaction {
            TimerDataTable.select { TimerDataTable.timerDataId eq timerDataId }.firstOrNull()?.let {
                serializeTimerData(it)
            }
        }
    }

    /**
     * [Guild]にある[TimerData]の一覧を取得
     *
     * @param guild [Guild]
     * @return [List]<[TimerData]>
     */
    fun getTimerData(guild: Guild): List<TimerData> {
        Connector.connect()

        //SELECT
        return transaction {
            TimerDataTable.select { TimerDataTable.guildId eq guild.idLong }.filterNotNull().map {
                serializeTimerData(it)
            }
        }
    }

    /**
     * トランザクションの[ResultRow]から[TimerData]を組み立てる
     *
     * @param resultRow 対象の[ResultRow]
     * @return [TimerData]
     */
    private fun serializeTimerData(resultRow: ResultRow): TimerData {
        //ひたすら代入
        return TimerData(
            resultRow[TimerDataTable.timerDataId],
            resultRow[TimerDataTable.channel],
            resultRow[TimerDataTable.number],
            resultRow[TimerDataTable.seconds],
            resultRow[TimerDataTable.displayMessageBase],
            TimerServiceData(
                resultRow[TimerDataTable.isStarted],
                resultRow[TimerDataTable.isMove],
                resultRow[TimerDataTable.isFinish],
                resultRow[TimerDataTable.startNanoTime],
                resultRow[TimerDataTable.adjustTime],
                resultRow[TimerDataTable.stopTime]
            )
        )
    }

    /**
     * レコード数をカウント
     *
     * @return 数
     */
    fun count(): Int {
        Connector.connect()

        return transaction {
            TimerDataTable.selectAll().fetchSize
        } ?: 0
    }

    /**
     * [TimerData]を削除する
     *
     * @param timerData 削除対象の[TimerData]
     */
    fun deleteTimerData(timerData: TimerData) {
        Connector.connect()

        //DELETE
        transaction {
            TimerDataTable.deleteWhere { channel.eq(timerData.channel) and number.eq(timerData.number) }
        }
    }
}