package dev.simpletimer.listener

import dev.simpletimer.database.transaction.TimerDataTransaction
import dev.simpletimer.extension.getTimerList
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
        val channel = event.channel.asGuildMessageChannel()
        channel.getTimerList().forEach {
            TimerDataTransaction.deleteTimerData(it.timerData)
        }
    }
}