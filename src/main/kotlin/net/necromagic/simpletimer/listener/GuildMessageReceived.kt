package net.necromagic.simpletimer.listener

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimer.*

/**
 * コマンドの処理のクラス
 *
 */
class GuildMessageReceived : ListenerAdapter() {

    /**
     * Guildからメッセージを受信した時に呼び出される
     *
     * @param event [MessageReceivedEvent] イベント
     */
    override fun onMessageReceived(event: MessageReceivedEvent) {

        // メッセージを取得
        val message = event.message

        //　ユーザーを確認
        val user = message.author
        if (user.isBot) {
            return
        }

        //誤字の修正
        val messageValue = message.contentRaw
            .replace("  ", " ")
            .replace("　", " ")
            .replace("　　", " ")
            .replace("！", "!")

        //stringからargsの生成
        val args = messageValue.split(" ")

        SimpleTimer.instance.commandManager.run(user, args, message)
    }
}