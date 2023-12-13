package dev.simpletimer.database.table

import dev.simpletimer.data.serializer.GuildMessageChannelSerializer
import dev.simpletimer.data.serializer.GuildSerializer
import dev.simpletimer.timer.Timer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb

/**
 * タイマーのキュー用のテーブル
 */
object TimerQueueTable : Table("timer_queue") {
    //対象のチャンネル
    val channel =
        jsonb<@Serializable(with = GuildMessageChannelSerializer::class) GuildMessageChannel>("channel", Json.Default)

    //番号
    val number = enumeration<Timer.Number>("number")

    //キューの一覧
    val queue = jsonb<List<Int>>("queue", Json.Default)

    //ギルド
    val guild = jsonb<@Serializable(with = GuildSerializer::class) Guild>("guild", Json.Default)

    override val primaryKey = PrimaryKey(channel, number)
}