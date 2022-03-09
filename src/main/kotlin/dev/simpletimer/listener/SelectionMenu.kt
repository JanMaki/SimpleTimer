package dev.simpletimer.listener

import dev.simpletimer.select_menu.SelectMenuManager
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * 選択メニューに対応をするクラス
 *
 */
class SelectionMenu : ListenerAdapter() {

    /**
     * 選択メニューを選択した時に実行される
     *
     * @param event [SelectMenuInteractionEvent] イベント
     */
    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        super.onSelectMenuInteraction(event)
        event.deferReply().queue({}, {})

        //すべての選択メニュー
        SelectMenuManager.selectMenus.forEach {
            //選択メニューを実行
            it.run(event)
        }
    }
}