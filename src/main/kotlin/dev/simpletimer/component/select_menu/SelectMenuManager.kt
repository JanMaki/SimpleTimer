package dev.simpletimer.component.select_menu

import dev.simpletimer.data.lang.lang_data.LangData
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

//選択メニューのマネージャー
object SelectMenuManager {
    //選択メニューの一覧
    val selectMenus = mutableSetOf<SelectMenu<*>>().apply {
        //選択メニューを追加
        addAll(
            arrayOf(
                DiceBotSelectMenu,
                ListSelectMenu
            )
        )
    }

    /**
     * 選択メニューの親
     *
     * @property name 識別に使う名前
     */
    abstract class SelectMenu<T>(val name: String, val deferReply: Boolean = true) {
        /**
         * 選択メニューを実行する
         *
         * @param event [StringSelectInteractionEvent] 選択をしたイベント
         */
        abstract fun run(event: StringSelectInteractionEvent)

        /**
         * 選択メニューを作成する
         *
         * @param data 選択メニューに何かデータつける時に使う
         * @return 作成した[SelectMenu]
         */
        abstract fun createSelectMenu(
            data: T,
            langData: LangData
        ): net.dv8tion.jda.api.interactions.components.selections.SelectMenu
    }
}