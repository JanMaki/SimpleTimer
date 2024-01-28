package dev.simpletimer.database.table

import dev.simpletimer.data.serializer.GuildMessageChannelSerializer
import dev.simpletimer.timer.Timer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb

/**
 * 現在動いているタイマーのテーブル
 *
 */
object TimerDataTable : Table("timer_data") {
    val timerDataId = long("timer_data_id").autoIncrement()

    //タイマーの基本データ
    val channel =
        jsonb<@Serializable(with = GuildMessageChannelSerializer::class) GuildMessageChannel>("channel", Json.Default)
    val number = enumeration<Timer.Number>("number")
    val seconds = integer("seconds")
    val displayMessageBase = text("timer_message_base").nullable()

    //ギルドのID
    val guildId = long("guild_id")

    //動作状況
    val isStarted = bool("is_started")
    val isMove = bool("is_move")
    val isFinish = bool("is_finish")
    val startMilliTime = long("start_milli_time")
    val adjustTime = long("adjust_time")
    val stopTime = long("stop_time")

    override val primaryKey = PrimaryKey(timerDataId)
}