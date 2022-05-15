package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.Emoji
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
        event.hook.sendMessage(event.guild!!.getLang().timer.timerMaxWarning).queue()
    }

    override fun createButton(data: Int, langData: LangData): Button {
        return Button.primary("${name}:$data", langData.component.button.startTime).withEmoji(Emoji.fromUnicode("⏱"))
    }
}