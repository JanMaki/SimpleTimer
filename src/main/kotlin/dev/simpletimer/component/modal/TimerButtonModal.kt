package dev.simpletimer.component.modal

import dev.simpletimer.component.button.TimerButton
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal

/**
 * タイマーのボタンのModal
 *
 */
object TimerButtonModal : TimerModal<Byte>("timer_button") {
    override fun run(event: ModalInteractionEvent, seconds: Int) {
        //言語のデータを取得
        val langData = event.guild?.getLang() ?: return

        //時間を確認する
        if (seconds < 1) {
            event.hook.sendMessage(langData.component.modal.moreThanOneWarning).setEphemeral(true).queue()
            return
        }

        //ボタンを送信
        event.hook.sendMessage(
            langData.component.modal.timerButton.langFormat(
                "**${
                    langData.timer.minus.langFormat(
                        seconds / 60
                    )
                }${langData.timer.minus.langFormat(seconds % 60)}**"
            )
        )
            .addActionRow(TimerButton.createButton(seconds, event.guild!!.getLang()))
            .queue()
    }

    override fun getModalBuilder(data: Byte, langData: LangData): Modal.Builder {
        return Modal.create("timer_button", langData.component.modal.createButton)
    }
}