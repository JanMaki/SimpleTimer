package dev.simpletimer.component.button

import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.extension.sendMessage
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.*

/**
 * タイマーを実行するボタン
 *
 */
object TimerButton : ButtonManager.Button<Int>("timer") {
    override fun run(event: ButtonInteractionEvent) {
        val channel: GuildMessageChannel = event.guildChannel as GuildMessageChannel

        //チャンネルのタイマーを取得する
        val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

        //番号を確認
        for (number in Timer.Number.values()) {
            //その番号のタイマーが動いているかを確認
            if (!channelTimers.containsKey(number)) {
                //タイマーを開始・代入
                channelTimers[number] = Timer(
                    channel,
                    number,
                    event.componentId.replace("${name}:", "").toIntOrNull() ?: return, event.guild!!
                )
                Timer.channelsTimersMap[channel] = channelTimers
                //空白を送信
                event.hook.sendEmpty()
                return
            }
        }

        //最大数のメッセージを出力する
        event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）", true).queue()
    }

    override fun createButton(data: Int): Button {
        return Button.primary("${name}:$data", "⏱開始")
    }
}