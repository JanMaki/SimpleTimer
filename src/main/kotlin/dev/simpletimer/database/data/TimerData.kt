package dev.simpletimer.database.data

import dev.simpletimer.timer.Timer
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

/**
 * タイマーの基本データ
 *
 * @property channel タイマーを動かすテキストチャンネル
 * @property number タイマーの番号
 * @property seconds 秒数
 * @property timerServiceData 稼働のデータ
 */
@Serializable
data class TimerData(
    var timerDataId: Long = -1,
    val channel: GuildMessageChannel,
    val number: Timer.Number,
    var seconds: Int,
    var displayMessageBase: String? = null,
    val timerServiceData: TimerServiceData = TimerServiceData()
)