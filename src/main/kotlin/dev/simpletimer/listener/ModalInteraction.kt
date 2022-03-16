package dev.simpletimer.listener

import dev.simpletimer.component.modal.ModalManager
import dev.simpletimer.util.equalsIgnoreCase
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

        //ModalのIDを取得
        val id = event.modalId

        //すべてのModal
        ModalManager.modals.filter { id.split(":")[0].equalsIgnoreCase(it.name) }.forEach {
            //考え中をするかを確認
            if (it.beforeReply) {
                //考え中を出す
                event.deferReply().queue({}, {})
            }

            //Modalを実行
            it.run(event)
        }
    }
}