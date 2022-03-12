package dev.simpletimer.component.modal

import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.util.*

/**
 * タイマーを開始するModal
 *
 */
object StartTimerModal : ModalInteractionManager.Modal<Byte>("start_timer") {
    override fun run(event: ModalInteractionEvent) {
        //入力した分数を取得
        val minutesInputValue = event.getValue("minutes_input")?.asString ?: ""

        //入力した秒数を取得
        val secondsInputValue = event.getValue("seconds_input")?.asString ?: ""

        //秒数を取得
        val seconds = ((minutesInputValue.toIntOrNull() ?: 0) * 60) + (secondsInputValue.toIntOrNull() ?: 0)

        //時間を確認する
        if (seconds <= 0) {
            event.hook.sendMessage("*1秒以上の時間を設定してください").queue({}, {})
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
                event.hook.sendMessage("|| ||").queue {
                    it.delete().queue({}, {})
                }
                return
            }
        }

        //最大数のメッセージを出力する
        event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）").queue({}, {})
    }

    //分数のInput
    private val minutesInput = TextInput.create("minutes_input", "分数", TextInputStyle.SHORT)
        .setPlaceholder("0")
        .setRequired(false)
        .build()

    //秒数のInput
    private val secondsInput = TextInput.create("seconds_input", "秒数", TextInputStyle.SHORT)
        .setPlaceholder("0")
        .setRequired(false)
        .build()

    override fun createModal(data: Byte): Modal {
        //Modalを作成して返す
        return Modal.create(name, "タイマーを開始する")
            .addActionRows(ActionRow.of(minutesInput), ActionRow.of(secondsInput))
            .build()
    }
}