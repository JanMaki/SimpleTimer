package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.*

/**
 * コマンドの処理のクラス
 *
 */
class GuildMessageReceived : ListenerAdapter() {

    /**
     * Guildからメッセージを受信した時に呼び出される
     *
     * @param event [GuildMessageReceivedEvent] イベント
     */
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        super.onGuildMessageReceived(event)

        //チャンネルごとの初期値を設定・取得
        val channel = event.channel

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

        SimpleTimer.instance.commandManager.run(user, channel, args, message)
    }

}