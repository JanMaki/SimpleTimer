package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import java.util.*

/**
 * タイマーを開始するModal
 *
 */
object StartTimerModal : TimerModal<Byte>("start_timer") {
    override fun run(event: ModalInteractionEvent, seconds: Int) {
        //言語のデータを取得
        val langData = event.guild?.getLang() ?: return

        //時間を確認する
        if (seconds < 1) {
            event.hook.sendMessage(langData.component.modal.moreThanOneWarning).setEphemeral(true).queue()
            return
        }

        //チャンネルを取得
        val channel = event.guildChannel as GuildMessageChannel

        //チャンネルのタイマーを取得する
        val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

        //番号を確認
        for (number in Timer.Number.values()) {
            //その番号のタイマーが動いているかを確認
            if (!channelTimers.containsKey(number)) {
                //タイマーを開始してインスタンスを代入する
                channelTimers[number] = Timer(channel, number, seconds, event.guild!!)
                Timer.channelsTimersMap[channel] = channelTimers

                //空白を出力して消し飛ばす
                event.hook.sendEmpty()
                return
            }
        }

        //最大数のメッセージを出力する
        event.hook.sendMessage(langData.timer.timerMaxWarning).queue()
    }

    override fun getModalBuilder(data: Byte, langData: LangData): Modal.Builder {
        //Modalを作成して返す
        return Modal.create(name, langData.component.modal.startTimer)
    }

}