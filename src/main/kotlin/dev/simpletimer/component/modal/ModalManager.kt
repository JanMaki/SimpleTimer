package dev.simpletimer.component.modal

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

/**
 * Modalのマネージャー
 */
object ModalManager {
    //Modalーの一覧
    val modals = mutableSetOf<Modal<*>>().apply {
        //Modalを追加
        addAll(
            arrayOf(
                AddTimerModal,
                DebugModal,
                QueueModal,
                StartTimerModal
            )
        )
    }

    /**
     * Modalの親
     *
     * @property name 識別に使う名前
     * @property beforeReply あらかじめbeforeReplyを実行するかどうか
     */
    abstract class Modal<T>(val name: String, val beforeReply: Boolean = true) {
        /**
         * Modalを実行する
         *
         * @param event [ModalInteractionEvent] イベント
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