package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import dev.simpletimer.timer.TimerQueue
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.Modal

/**
 * キューに追加をするモーダル
 *
 */
object QueueModal : TimerModal<Timer.Number>("queue") {
    override fun run(event: ModalInteractionEvent, seconds: Int) {
        //時間を確認する
        if (seconds < 1) {
            event.hook.sendMessage(event.guild!!.getLang().component.modal.moreThanOneWarning).queue()
            return
        }

        val timerNumber = Timer.Number.getNumber(event.modalId.split(":")[1].toInt()) ?: Timer.Number.FIRST

        //キューを取得
        val queue = TimerQueue.getTimerQueue(event.guild!!, event.guildChannel as GuildMessageChannel, timerNumber)

        //キューに追加
        queue.addTimerQueue(seconds)

        //キューの量を確認
        if (queue.getQueue().size > 0) {
            //メッセージを送信
            event.hook.sendMessageEmbeds(queue.getQueueEmbed(event.guild!!.getLang())).queue()
        } else {
            //空のメッセージを送信
            event.hook.sendEmpty()
        }
    }

    override fun getModalBuilder(data: Timer.Number, langData: LangData): Modal.Builder {
        //Modalを作成して返す
        return Modal.create("queue:${data.number}", langData.component.modal.addQueue)
    }
}