package dev.simpletimer.component.modal

import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.extension.sendMessageEmbeds
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

        val timerNumber = Timer.Number.getNumber(event.modalId.split(":")[1].toInt()) ?: Timer.Number.FIRST

        //キューを取得
        val queue = TimerQueue.getTimerQueue(event.guild!!, event.guildChannel as GuildMessageChannel, timerNumber)

        //キューに追加
        queue.addTimerQueue(seconds)

        //キューの量を確認
        if (queue.getQueue().size > 0) {
            //メッセージを送信
            event.hook.sendMessageEmbeds(queue.getQueueEmbed(), true).queue()
        } else {
            //空のメッセージを送信
            event.hook.sendEmpty()
        }
    }

    override fun getModalBuilder(data: Timer.Number): Modal.Builder {
        //Modalを作成して返す
        return Modal.create("queue:${data.number}", "${data.number}番目のタイマーにキューを追加")
    }
}