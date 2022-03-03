package dev.simpletimer

import dev.simpletimer.data.getGuildData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu

object TimerList {
    /**
     * タイマーリストを送信する
     *
     * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
     * @author Shiba_Magic
     */
    fun sendList(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!

        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        var appendMessage = ""

        if (guildData.listSync) {
            val targetGuild = guildData.syncTarget
            appendMessage = if (targetGuild != null) {
                "この一覧は同期されています"
            } else {
                "同期元のサーバーが見つからなかったため、このサーバーに設定されている一覧を表示しています"
            }
        }

        //メニューを作成
        val selectionMenu = SelectMenu.create("TimerList")
        selectionMenu.placeholder = "タイマーを選択"
        selectionMenu.setRequiredRange(1, 1)

        //ギルドのデータからタイマーの一覧を取得
        val timerList = guildData.timerList

        //埋め込みを作成開始
        val builder = EmbedBuilder()
        //タイトルを設定
        builder.setTitle("タイマー一覧")

        builder.setDescription(appendMessage)

        //タイマの一覧を回す
        timerList.forEach {
            //埋め込みのフィールドに追加
            builder.addField(it.key, "　${it.value}分", false)

            //メニューに追加
            selectionMenu.addOption("${it.key} (${it.value}分)", "${it.key}:${it.value}")
        }


        if (timerList.isEmpty()) {
            //空だった時は埋め込みだけ送信
            event.hook.sendMessageEmbeds(builder.build()).queue({}, {})
        } else {
            //中身があるときは、埋め込みとメニューを送信
            event.hook.sendMessageEmbeds(builder.build())
                .setEphemeral(true)
                .addActionRow(selectionMenu.build())
                .queue({}, {})
        }
    }
}