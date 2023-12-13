package dev.simpletimer.timer

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.database.transaction.TimerQueueTransaction
import dev.simpletimer.extension.getTimerList
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * キュー周りの処理
 *
 * @property channel 対象のチャンネル
 * @property number 対象の[Timer.Number]
 *
 * @param queue キューの内容
 */
class TimerQueue(val channel: GuildMessageChannel, val number: Timer.Number, queue: List<Int> = mutableListOf()) :
    TimerService.TimerListener {
    companion object {
        //キューのマップ
        private val timerQueues = ConcurrentHashMap<Long, ConcurrentHashMap<Timer.Number, TimerQueue>>()

        /**
         * チャンネルとギルドとナンバーからタイマーのキューを取得する
         *
         * @param channel [GuildMessageChannel]
         * @param number [Timer.Number]
         * @return [TimerQueue]を返す
         */
        fun getTimerQueue(channel: GuildMessageChannel, number: Timer.Number): TimerQueue {
            return timerQueues.getOrPut(channel.idLong) { ConcurrentHashMap() }
                .getOrPut(number) { TimerQueue(channel, number) }
        }

        /**
         * タイマーキューを登録する
         *
         * @param channel [GuildMessageChannel]
         * @param number [Timer.Number]
         * @param queue キューの内容
         */
        fun registerTimerQueue(channel: GuildMessageChannel, number: Timer.Number, queue: List<Int>) {
            //mapに追加
            timerQueues.getOrPut(channel.idLong) { ConcurrentHashMap() }[number] = TimerQueue(channel, number, queue)
        }
    }

    init {
        TimerQueueTransaction.upsertQueue(channel, number, queue)
    }

    /**
     * キューを追加する
     *
     * @param seconds 秒
     */
    fun addTimerQueue(seconds: Int) {
        //チャンネルのタイマーを取得する
        val channelTimers = channel.getTimerList()

        //タイマーが動いているかを確認
        if (channelTimers.any { it.timerData.number == number }) {
            //キューに追加
            TimerQueueTransaction.getQueue(channel, number).toMutableList().apply {
                add(seconds)
            }.let {
                TimerQueueTransaction.upsertQueue(channel, number, it)
            }
        } else {
            //タイマーを開始し、インスタンスを代入する
            Timer(channel, number, seconds).start()
        }
    }

    /**
     * キューを空にする
     *
     */
    fun clearQueue() {
        TimerQueueTransaction.clear(channel, number)
    }

    /**
     * Indexからキューを削除
     *
     * @param index インデックス
     */
    fun removeQueueIndex(index: Int) {
        //キューを取得
        TimerQueueTransaction.getQueue(channel, number).toMutableList().apply {
            //Indexを指定して削除
            removeAt(index)
        }.let {
            //削除後のキューの大きさを確認
            if (it.size <= 0) {
                //消し飛ばす
                clearQueue()
            } else {
                //キューの内容を更新
                TimerQueueTransaction.upsertQueue(channel, number, it)
            }
        }
    }

    /**
     * キューを取得
     *
     * @return [List]<[Int]>
     */
    fun getQueue(): List<Int> {
        return TimerQueueTransaction.getQueue(channel, number)
    }

    override fun onFinish(check: Boolean) {
        val queue = TimerQueueTransaction.getQueue(channel, number).toMutableList()

        //キューがあるかを確認
        if (queue.isEmpty()) return

        //タイマーを作成し、開始させる
        Timer(channel, number, queue[0]).start()

        //キューを削除
        removeQueueIndex(0)
    }


    /**
     * キューの埋め込みを作成する
     *
     * @return 作成した[MessageEmbed]
     */
    fun getQueueEmbed(langData: LangData): MessageEmbed {
        val embed = EmbedBuilder()

        //タイトルを設定
        embed.setTitle(langData.timer.queueNumber.langFormat(number.number))

        //キューを確認
        getQueue().withIndex().forEach { (index, totalSeconds) ->
            //フィールドを追加
            embed.addField(
                "${index + 1}",
                "${langData.timer.minutes.langFormat(totalSeconds / 60)}${langData.timer.seconds.langFormat(totalSeconds % 60)}",
                true
            )
        }

        //作って返す
        return embed.build()
    }


    override fun onStart() {
        //何もしない
    }

    override fun onStop(check: Boolean) {
        //何もしない
    }

    override fun onRestart(check: Boolean) {
        //何もしない
    }

    override fun onEnd(check: Boolean) {
        //何もしない
    }

    override fun onAdd(seconds: Int) {
        //何もしない
    }

    override fun onUpdate() {
        //何もしない
    }
}