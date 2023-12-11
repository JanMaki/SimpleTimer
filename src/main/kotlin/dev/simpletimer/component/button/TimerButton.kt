package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * タイマーを実行するボタン
 *
 */
object TimerButton : ButtonManager.Button<Int>("timer") {
    override fun run(event: ButtonInteractionEvent) {
        //チャンネルを取得
        val channel = event.channel.asGuildMessageChannel()

        //使用できる番号を取得
        val usableNumber = Timer.getUsableNumber(channel)

        //埋まっているかを確認
        if (usableNumber.isEmpty()) {
            //最大数のメッセージを送信
            event.hook.sendMessage(event.guild!!.getLang().timer.timerMaxWarning).queue()
            return
        }

        //秒数単位にする
        val seconds = event.componentId.replace("${name}:", "").toIntOrNull() ?: return

        //タイマーを開始
        Timer(channel, usableNumber.first(), seconds).start()

        //空白を送信して終了
        event.hook.sendEmpty()
    }

    override fun createButton(data: Int, langData: LangData): Button {
        return Button.primary("${name}:$data", langData.component.button.startTime).withEmoji(Emoji.fromUnicode("⏱"))
    }
}