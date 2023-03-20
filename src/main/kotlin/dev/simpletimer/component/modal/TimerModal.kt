package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

abstract class TimerModal<T>(name: String, deferReply: Boolean = true) :
    ModalManager.Modal<T>(name, deferReply = deferReply) {
    override fun run(event: ModalInteractionEvent) {
        //入力した分数を取得
        val minutesInputValue = event.getValue("minutes_input")?.asString ?: ""

        //入力した秒数を取得
        val secondsInputValue = event.getValue("seconds_input")?.asString ?: ""

        //秒数を確認
        if (((minutesInputValue.toLongOrNull() ?: 0) * 60) + (secondsInputValue.toLongOrNull() ?: 0) > Int.MAX_VALUE) {
            //メッセージを取得
            val message = event.guild!!.getLang().component.modal.missingTimeWarning
            //deferReplyを確認
            if (deferReply) {
                //メッセージを送信
                event.hook.sendMessage(message).queue()
            } else {
                //eventに返す
                event.reply(message).queue()
            }

            return
        }

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
            .addActionRow(minutesInput).addActionRow(secondsInput)
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