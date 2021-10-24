package net.necromagic.simpletimerKT

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu

object TimerList {
    /**
     * タイマーリストを送信する
     *
     * @param event [SlashCommandEvent] スラッシュコマンドのイベント
     * @author Shiba_Magic
     */
    fun sendList(event: SlashCommandEvent) {
        //メニューを作成
        val selectionMenu = SelectionMenu.create("TimerList")
        selectionMenu.placeholder = "タイマーを選択"
        selectionMenu.setRequiredRange(1, 1)

        //コンフィグからタイマーの一覧を取得
        val timerList = SimpleTimer.instance.config.getTimerList(event.guild!!)

        //埋め込みを作成開始
        val builder = EmbedBuilder()
        //タイトルを設定
        builder.setTitle("タイマー一覧")

        //タイマの一覧を回す
        timerList.forEach {
            //埋め込みのフィールドに追加
            builder.addField(it.key, "　${it.value}分", false)

            //メニューに追加
            selectionMenu.addOption("${it.key} (${it.value}分)", "${it.key}:${it.value}")
        }


        if (timerList.isEmpty()) {
            //空だった時は埋め込みだけ送信
            event.hook.sendMessageEmbeds(builder.build()).queue()
        } else {
            //中身があるときは、埋め込みとメニューを送信
            event.hook.sendMessageEmbeds(builder.build())
                .setEphemeral(true)
                .addActionRow(selectionMenu.build())
                .complete()
        }
    }
}