package dev.simpletimer.listener

import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * チャンネルの削除に対応をするクラス
 *
 */
class ChannelDelete : ListenerAdapter() {
    /**
     * チャンネルを削除したときに呼び出される
     *
     * @param event [ChannelDeleteEvent]
     */
    override fun onChannelDelete(event: ChannelDeleteEvent) {
        //タイマーを取得
        Timer.getTimers(event.channel).values.forEach { timer ->
            //終了フラグを立てる
            timer.timerService.isFinish = true

            //ディスプレイのデータを削除
            val display = timer.display
            Timer.displays.remove(display?.idLong)
            Timer.timers.remove(display?.idLong)

            //通知のデータを削除
            val notice = timer.notice
            Timer.timers.remove(notice?.idLong)

            //チャンネルのタイマーから削除
            Timer.channelsTimersMap[event.channel]?.remove(timer.number)
        }
    }
}