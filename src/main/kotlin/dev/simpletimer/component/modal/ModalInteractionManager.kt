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
    interface Modal<T> {
        /**
         * Modalを実行する
         *
         * @param event [ModalInteractionEvent] 選択をしたイベント
         */
        fun run(event: ModalInteractionEvent)

        /**
         * Modalを作成する
         *
         * @param data Modalに何かデータつける時に使う
         * @return 作成した[Modal]
         */
        fun createModal(data: T): net.dv8tion.jda.api.interactions.components.text.Modal
    }
}