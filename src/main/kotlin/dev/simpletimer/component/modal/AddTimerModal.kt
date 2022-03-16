package dev.simpletimer.component.modal

import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.Modal
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * タイマーを延長するModal
 *
 */
object AddTimerModal : TimerModal<Timer.Number>("add_timer", false) {
    override val minutesInputName: String
        get() = "延長する分数"
    override val secondsInputName: String
        get() = "延長する秒数"

    override fun run(event: ModalInteractionEvent, seconds: Int) {
        val timerNumber = Timer.Number.getNumber(event.modalId.split(":")[1].toInt())

        //チャンネルを取得
        val channel = event.messageChannel

        //チャンネルのタイマーを取得
        val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

        //タイマーの稼働を確認
        if (!channelTimers.containsKey(timerNumber)) {
            if (timerNumber != null)
                event.reply(timerNumber.format("タイマーは動いていません")).queue({}, {})
            else {
                event.reply("タイマーは動いていません").queue({}, {})
            }
            return
        }

        //タイマーを取得
        val timer = channelTimers[timerNumber]!!

        //タイマーを延長
        timer.add(seconds)

        //空白を出力して消し飛ばす
        event.deferReply().queue {
            event.hook.sendMessage("|| ||").queueAfter(1L, TimeUnit.NANOSECONDS) {
                it.delete().queue({}, {})
            }
        }
    }

    override fun getModalBuilder(data: Timer.Number): Modal.Builder {
        return Modal.create("add_timer:${data.number}", "${data.number}番目のタイマーを延長する")
    }
}