package dev.simpletimer.listener

import dev.simpletimer.component.select_menu.SelectMenuManager
import dev.simpletimer.util.equalsIgnoreCase
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * 選択メニューに対応をするクラス
 *
 */
class SelectMenuInteraction : ListenerAdapter() {

    /**
     * 選択メニューを選択した時に実行される
     *
     * @param event [SelectMenuInteractionEvent] イベント
     */
    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        super.onSelectMenuInteraction(event)
        event.deferReply().queue({}, {})

        //選択メニューのIDを取得
        val id = event.selectMenu.id ?: return

        //すべての選択メニュー
        SelectMenuManager.selectMenus.filter { id.split(":")[0].equalsIgnoreCase(it.name) }.forEach {
            //選択メニューを実行
            it.run(event)
        }
    }
}