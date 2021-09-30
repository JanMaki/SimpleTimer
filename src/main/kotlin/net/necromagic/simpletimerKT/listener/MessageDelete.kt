package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.Timer

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

        //タイマーを取得
        val timer = Timer.getTimer(idLong) ?: return

        //ディスプレイ用メッセージか確認
        if (!Timer.isDisplay(idLong)) return

        //タイマーを停止
        timer.stop()

        //通知用のメッセージの場合はむ無視
        val message = timer.notice ?: return

        //操作用リアクションを追加
        message.addReaction("\uD83D\uDED1").queue({}, {})
        message.addReaction("❌").queue({}, {})
    }
}