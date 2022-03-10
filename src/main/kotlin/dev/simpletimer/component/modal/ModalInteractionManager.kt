package dev.simpletimer.component.modal

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

/**
 * Modalのマネージャー
 */
object ModalInteractionManager {
    //Modalーの一覧
    val modals = mutableSetOf<Modal<*>>().apply {
        //Modalを追加
        addAll(
            arrayOf(
                DebugModal
            )
        )
    }

    /**
     * Modalの親
     *
     */
    abstract class Modal<T>(val name: String) {
        /**
         * Modalを実行する
         *
         * @param event [ModalInteractionEvent] 選択をしたイベント
         */
        abstract fun run(event: ModalInteractionEvent)

        /**
         * Modalを作成する
         *
         * @param data Modalに何かデータつける時に使う
         * @return 作成した[Modal]
         */
        abstract fun createModal(data: T): net.dv8tion.jda.api.interactions.components.text.Modal
    }
}