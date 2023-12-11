package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.modals.Modal

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
        val channel = event.channel.asGuildMessageChannel()

        //使用できる番号を取得
        val usableNumber = Timer.getUsableNumber(channel)

        //埋まっているかを確認
        if (usableNumber.isEmpty()) {
            //最大数のメッセージを送信
            event.hook.sendMessage(event.guild!!.getLang().timer.timerMaxWarning).queue()
            return
        }

        //タイマーを開始
        Timer(channel, usableNumber.first(), seconds).start()

        //空白を送信して終了
        event.hook.sendEmpty()
    }

    override fun getModalBuilder(data: Byte, langData: LangData): Modal.Builder {
        //Modalを作成して返す
        return Modal.create(name, langData.component.modal.startTimer)
    }

}