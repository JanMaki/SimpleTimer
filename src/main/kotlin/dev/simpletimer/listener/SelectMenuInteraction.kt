package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.select_menu.SelectMenuManager
import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.extension.equalsIgnoreCase
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * 選択メニューに対応をするクラス
 *
 */
class SelectMenuInteraction : ListenerAdapter() {

    /**
     * 選択メニューを選択した時に実行される
     *
     * @param event [StringSelectInteractionEvent] イベント
     */
    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        super.onStringSelectInteraction(event)

        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            //権限が不足しているメッセージを送信する
            event.replyEmbeds(SimpleTimer.instance.getErrorEmbed(event.guildChannel)).queue()
            return
        }

        //選択メニューのIDを取得
        val id = event.selectMenu.id ?: return

        //すべての選択メニュー
        SelectMenuManager.selectMenus.filter { id.split(":")[0].equalsIgnoreCase(it.name) }.forEach {
            //考え中をするかを確認
            if (it.deferReply) {
                //考え中を出す
                event.deferReply().queue()
            }

            //選択メニューを実行
            it.run(event)
        }
    }
}