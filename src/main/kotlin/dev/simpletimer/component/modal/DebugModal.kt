package dev.simpletimer.component.modal

import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

/**
 * デバッグ機能のModal
 *
 */
object DebugModal : ModalInteractionManager.Modal<Byte> {
    override fun run(event: ModalInteractionEvent) {
        //メニューのID
        val id = event.modalId

        //IDを確認
        if (id != "debug") return

        //入力した値を取得
        val inputValue = event.getValue("input")?.asString ?: return

        //値を確認
        when (inputValue) {
            //タイマーの稼働数を確認する
            "count" -> {
                //タイマーの稼働数を送信する
                event.hook.sendMessage("${Timer.getCount()}個のタイマーが稼働しています").queue({}, {})
            }
            //無効な値の時
            else -> {
                //メッセージを送信
                event.hook.sendMessage("*無効な入力です").queue({}, {})
            }
        }
    }

    override fun createModal(data: Byte): Modal {
        //TextInputを作成
        val input = TextInput.create("input", "入力", TextInputStyle.SHORT)
            .setPlaceholder("値を入力してください")
            .setRequired(true)
            .build()
        //Modalを作成して返す
        return Modal.create("debug", "Debug").addActionRow(input).build()
    }
}