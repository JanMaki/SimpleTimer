package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

abstract class TimerModal<T>(name: String, beforeReply: Boolean = true) :
    ModalManager.Modal<T>(name, deferReply = beforeReply) {
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

    override fun createModal(data: T, langData: LangData): Modal {
        //Inputの名前のペアを取得
        val inputNames = getInputNames(langData)

        //分数のInput
        val minutesInput: TextInput = TextInput.create("minutes_input", inputNames.first, TextInputStyle.SHORT)
            .setPlaceholder("0")
            .setRequired(false)
            .build()

        //秒数のInput
        val secondsInput = TextInput.create("seconds_input", inputNames.second, TextInputStyle.SHORT)
            .setPlaceholder("0")
            .setRequired(false)
            .build()

        //Modalを作成して返す
        return getModalBuilder(data, langData)
            .addActionRows(ActionRow.of(minutesInput), ActionRow.of(secondsInput))
            .build()
    }

    /**
     * Inputの名前を取得する
     *
     * @param langData　[LangData]言語のデータ
     * @return 分と秒のペア
     */
    open fun getInputNames(langData: LangData): Pair<String, String> {
        return Pair(langData.component.modal.minutes, langData.component.modal.seconds)
    }


    /**
     * ModalBuilderを取得する
     *
     * @param data Modalに何かデータつける時に使う
     * @return [Modal.Builder]
     */
    abstract fun getModalBuilder(data: T, langData: LangData): Modal.Builder
}