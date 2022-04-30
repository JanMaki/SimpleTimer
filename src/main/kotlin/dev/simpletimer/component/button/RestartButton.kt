package dev.simpletimer.component.button

import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

object RestartButton : ButtonManager.Button<Timer.Number>("restart_timer") {
    override fun run(event: ButtonInteractionEvent) {
        //タイマーの番号を取得
        val timerNumber = Timer.Number.getNumber(event.button.id!!.split(":")[1].toInt()) ?: return
        //timerを取得
        val timer = Timer.getTimers(event.guildChannel)[timerNumber] ?: return
        //再開
        timer.restart()
        //ボタンを変更
        event.editButton(StopButton.createButton(timerNumber)).queue()
        //空白を送信
        event.hook.sendEmpty()
    }

    override fun createButton(data: Timer.Number): Button {
        //ボタンを作成して返す
        return Button.primary("restart_timer:${data.number}", "再開")
    }
}