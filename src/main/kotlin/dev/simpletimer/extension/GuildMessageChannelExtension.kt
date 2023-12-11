package dev.simpletimer.extension

import dev.simpletimer.database.transaction.TimerDataTransaction
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

/**
 * [GuildMessageChannel]の拡張
 * [Number]からそのチャンネルで動いているタイマーを取得する
 *
 * @param number 取得するタイマーの[Number]
 * @return [Timer]?
 */
fun GuildMessageChannel.getTimer(number: Timer.Number?): Timer? {
    if (number == null) return null

    //タイマーを取得
    val timerData = TimerDataTransaction.getTimerData(this, number) ?: return null
    return Timer.getTimerFromTimerDataId(timerData.timerDataId)
}

/**
 * [GuildMessageChannel]の拡張
 * チャンネルで動いているタイマーを取得する
 *
 * @return [List]<[Timer]>
 */
fun GuildMessageChannel.getTimerList(): List<Timer> {
    //すべてのタイマーのデータを取得してnullを除去して返す
    return TimerDataTransaction.getTimerData(this).mapNotNull {
        Timer.getTimerFromTimerDataId(it.timerDataId)
    }
}