package dev.simpletimer.listener

import dev.simpletimer.component.modal.ModalInteractionManager
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Modal対応をするクラス
 *
 */
class ModalInteraction : ListenerAdapter() {

    /**
     * 選択メニューを選択した時に実行される
     * @param event [ModalInteractionEvent] イベント
     */
    override fun onModalInteraction(event: ModalInteractionEvent) {
        super.onModalInteraction(event)
        event.deferReply().queue()

        //ModalのIDを取得
        val id = event.modalId

        //すべてのModal
        ModalInteractionManager.modals.filter { it.name.startsWith(id) }.forEach {
            //Modalを実行
            it.run(event)
        }
    }
}