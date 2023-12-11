package dev.simpletimer.listener

import dev.simpletimer.database.transaction.TimerMessageTransaction
import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * メッセージ削除時に対応をするクラス
 *
 */
class MessageDelete : ListenerAdapter() {

    /**
     * メッセージが削除された時に呼び出される
     *
     * @param event [MessageDeleteEvent] イベント
     */
    override fun onMessageDelete(event: MessageDeleteEvent) {
        super.onMessageDelete(event)

        val idLong = event.messageIdLong

        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            return
        }

        //ディスプレイ用メッセージか確認
        if (!TimerMessageTransaction.isDisplayMessage(idLong)) return


        //タイマーを取得
        val timerData = TimerMessageTransaction.getTimerDataByMessage(idLong) ?: return
        val timer = Timer.getTimerFromTimerDataId(timerData.timerDataId) ?: return

        //タイマーを終了
        timer.end()
    }
}