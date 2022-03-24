package dev.simpletimer.component.modal

import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.extension.sendMessage
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import java.util.*

/**
 * タイマーを開始するModal
 *
 */
object StartTimerModal : TimerModal<Byte>("start_timer") {
    override val minutesInputName: String
        get() = "分数"
    override val secondsInputName: String
        get() = "秒数"

    override fun run(event: ModalInteractionEvent, seconds: Int) {
        //時間を確認する
        if (seconds < 1) {
            event.hook.sendMessage("*1秒以上の時間を設定してください").setEphemeral(true).queue()
            return
        }

        //チャンネルを取得
        val channel = event.messageChannel

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
        event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）", true).queue()
    }

    override fun getModalBuilder(data: Byte): Modal.Builder {
        //Modalを作成して返す
        return Modal.create(name, "タイマーを開始する")
    }

}