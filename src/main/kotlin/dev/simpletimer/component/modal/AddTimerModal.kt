package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal
import java.util.*

/**
 * タイマーを延長するModal
 *
 */
object AddTimerModal : TimerModal<Timer.Number>("add_timer", false) {
    override fun run(event: ModalInteractionEvent, seconds: Int) {
        //言語のデータを取得
        val langData = event.guild?.getLang() ?: return

        //時間を確認する
        if (seconds == 0) {
            event.reply(langData.component.modal.missingTimeWarning).queue()
            return
        }

        val timerNumber = Timer.Number.getNumber(event.modalId.split(":")[1].toInt())

        //チャンネルを取得
        val channel = event.messageChannel

        //チャンネルのタイマーを取得
        val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

        //タイマーの稼働を確認
        if (!channelTimers.containsKey(timerNumber)) {
            if (timerNumber != null)
                event.reply(timerNumber.format(langData.timer.timerNotMoveWarning)).queue()
            else {
                event.reply(langData.timer.timerNotMoveWarning).queue()
            }
            return
        }

        //タイマーを取得
        val timer = channelTimers[timerNumber]!!

        //タイマーを延長
        timer.add(seconds)

        //空白を出力して消し飛ばす
        event.deferReply().queue {
            event.hook.sendEmpty()
        }
    }

    override fun getInputNames(langData: LangData): Pair<String, String> {
        return Pair(langData.component.modal.addTimeMinutes, langData.component.modal.addTimeSeconds)
    }

    override fun getModalBuilder(data: Timer.Number, langData: LangData): Modal.Builder {
        return Modal.create("add_timer:${data.number}", langData.component.modal.addTime.langFormat(data.number))
    }
}