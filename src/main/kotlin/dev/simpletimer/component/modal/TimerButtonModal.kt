package dev.simpletimer.component.modal

import dev.simpletimer.component.button.TimerButton
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.Modal

/**
 * タイマーのボタンのModal
 *
 */
object TimerButtonModal : TimerModal<Byte>("timer_button") {
    override fun run(event: ModalInteractionEvent, seconds: Int) {
        //時間を確認する
        if (seconds < 1) {
            event.hook.sendMessage("*1秒以上の時間を設定してください").setEphemeral(true).queue()
            return
        }

        //ボタンを送信
        event.hook.sendMessage("**${seconds / 60}分${seconds % 60}秒**のタイマーを開始する")
            .addActionRow(TimerButton.createButton(seconds))
            .queue()
    }

    override val minutesInputName: String
        get() = "分"
    override val secondsInputName: String
        get() = "秒"

    override fun getModalBuilder(data: Byte): Modal.Builder {
        return Modal.create("timer_button", "ボタンを作成")
    }
}