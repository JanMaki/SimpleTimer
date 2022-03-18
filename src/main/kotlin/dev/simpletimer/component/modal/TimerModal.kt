package dev.simpletimer.component.modal

import dev.simpletimer.extension.sendMessage
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

abstract class TimerModal<T>(name: String, beforeReply: Boolean = true) :
    ModalManager.Modal<T>(name, beforeReply = beforeReply) {
    override fun run(event: ModalInteractionEvent) {
        //入力した分数を取得
        val minutesInputValue = event.getValue("minutes_input")?.asString ?: ""

        //入力した秒数を取得
        val secondsInputValue = event.getValue("seconds_input")?.asString ?: ""

        //秒数を取得
        val seconds = ((minutesInputValue.toIntOrNull() ?: 0) * 60) + (secondsInputValue.toIntOrNull() ?: 0)

        //実行
        run(event, seconds)
    }

    /**
     * 時間と一緒にModalを実行する
     *
     * @param event [ModalInteractionEvent] イベント
     * @param seconds 秒数
     */
    abstract fun run(event: ModalInteractionEvent, seconds: Int)

    //分数のInputの名前
    abstract val minutesInputName: String

    //秒数のInputの名前
    abstract val secondsInputName: String

    //分数のInput
    private val minutesInput: TextInput = TextInput.create("minutes_input", minutesInputName, TextInputStyle.SHORT)
        .setPlaceholder("0")
        .setRequired(false)
        .build()

    //秒数のInput
    private val secondsInput = TextInput.create("seconds_input", secondsInputName, TextInputStyle.SHORT)
        .setPlaceholder("0")
        .setRequired(false)
        .build()


    override fun createModal(data: T): Modal {
        //Modalを作成して返す
        return getModalBuilder(data)
            .addActionRows(ActionRow.of(minutesInput), ActionRow.of(secondsInput))
            .build()
    }

    /**
     * ModalBuilderを取得する
     *
     * @param data Modalに何かデータつける時に使う
     * @return [Modal.Builder]
     */
    abstract fun getModalBuilder(data: T): Modal.Builder
}