package dev.simpletimer.select_menu

import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

//選択メニューのマネージャー
object SelectMenuManager {
    //選択メニューの一覧
    val selectMenus = mutableSetOf<SelectMenu<*>>().apply {
        //選択メニューを追加
        addAll(
            arrayOf(
                ListSelectMenu
            )
        )
    }

    /**
     * 選択メニューの親
     *
     */
    interface SelectMenu<T> {
        /**
         * 選択メニューを実行する
         *
         * @param event [SelectMenuInteractionEvent] 選択をしたイベント
         */
        fun run(event: SelectMenuInteractionEvent)

        /**
         * 選択メニューを作成する
         *
         * @param data 選択メニューに何かデータつける時に使う
         * @return 作成した[SelectMenu]
         */
        fun createSelectMenu(data: T): net.dv8tion.jda.api.interactions.components.selections.SelectMenu
    }
}