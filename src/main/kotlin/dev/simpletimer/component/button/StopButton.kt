package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

object StopButton : ButtonManager.Button<Timer.Number>("stop_timer") {
    override fun run(event: ButtonInteractionEvent) {
        //タイマーの番号を取得
        val timerNumber = Timer.Number.getNumber(event.button.id!!.split(":")[1].toInt()) ?: return
        //timerを取得
        val timer = Timer.getTimers(event.guildChannel)[timerNumber] ?: return
        //停止
        timer.stop()
        //ボタンを変更
        event.editButton(RestartButton.createButton(timerNumber, event.guild!!.getLang())).queue()
        //空白を送信
        event.hook.sendEmpty()
    }

    override fun createButton(data: Timer.Number, langData: LangData): Button {
        //ボタンを作成して返す
        return Button.primary("stop_timer:${data.number}", langData.component.button.stopTimer)
            .withEmoji(Emoji.fromUnicode("⏸️"))
    }
}