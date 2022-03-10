package dev.simpletimer.component.modal

import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * タイマーを延長するModal
 *
 */
object AddTimerModal : ModalInteractionManager.Modal<Timer.Number>("add_timer", false) {
    override fun run(event: ModalInteractionEvent) {
        //入力した分数を取得
        val minutesInputValue = event.getValue("minutes_input")?.asString ?: ""

        //入力した秒数を取得
        val secondsInputValue = event.getValue("seconds_input")?.asString ?: ""

        //秒数を取得
        val seconds = ((minutesInputValue.toIntOrNull() ?: 0) * 60) + (secondsInputValue.toIntOrNull() ?: 0)

        //時間を確認する
        if (seconds == 0) {
            event.reply("*時間を設定してください").queue({}, {})
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

    //分数のInput
    private val minutesInput = TextInput.create("minutes_input", "延長する分数", TextInputStyle.SHORT)
        .setPlaceholder("0")
        .setRequired(false)
        .build()

    //秒数のInput
    private val secondsInput = TextInput.create("seconds_input", "延長する秒数", TextInputStyle.SHORT)
        .setPlaceholder("0")
        .setRequired(false)
        .build()

    override fun createModal(data: Timer.Number): Modal {
        //Modalを作成して返す
        return Modal.create("add_timer:${data.number}", "${data.number}番目のタイマーを延長する")
            .addActionRows(ActionRow.of(minutesInput), ActionRow.of(secondsInput))
            .build()
    }
}