package dev.simpletimer.component.button

import dev.simpletimer.component.modal.AddTimerModal
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * タイマーを延長するボタン
 *
 */
object AddTimerButton : ButtonManager.Button<Timer.Number>("add_timer", false) {
    override fun run(event: ButtonInteractionEvent) {
        //タイマーの番号を取得
        val timerNumber = Timer.Number.getNumber(event.button.id!!.split(":")[1].toInt()) ?: return
        //Modalを送信
        event.replyModal(AddTimerModal.createModal(timerNumber, event.guild!!.getLang())).queue()
    }

    override fun createButton(data: Timer.Number, langData: LangData): Button {
        //ボタンを作成して返す
        return Button.primary("add_timer:${data.number}", langData.component.button.addTimer)
            .withEmoji(Emoji.fromUnicode("➕"))
    }
}