package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.sendEmpty
import net.dv8tion.jda.api.entities.Emoji
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

    override fun createButton(data: Byte, langData: LangData): Button {
        return Button.secondary("delete", Emoji.fromUnicode("\uD83D\uDDD1"))
    }
}