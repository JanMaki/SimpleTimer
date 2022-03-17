package dev.simpletimer.component.button

import dev.simpletimer.util.sendEmpty
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * ボタンがついているメッセージを削除する
 *
 */
object DeleteMessageButton : ButtonManager.Button<Byte>("delete") {
    override fun run(event: ButtonInteractionEvent) {
        //メッセージ削除
        event.message.delete().queue()
        //空白を送信
        event.hook.sendEmpty()
    }

    override fun createButton(data: Byte): Button {
        return Button.secondary("delete", "🗑️")
    }
}