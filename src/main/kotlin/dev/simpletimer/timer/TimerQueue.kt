package dev.simpletimer.timer

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.*

/**
 * キューを管理する
 *
 * @constructor Create empty Queue service
 */
class TimerQueue(val guild: Guild, val channel: GuildMessageChannel, val number: Timer.Number) :
    TimerService.TimerListener {
    companion object {
        private val timerQueues = mutableMapOf<Long, MutableMap<Timer.Number, TimerQueue>>()

        /**
         * チャンネルとギルドとナンバーからタイマーのキューを取得する
         *
         * @param guild [Guild]
         * @param channel [MessageChannel]
         * @param number [Timer.Number]
         * @return [TimerQueue]を返す
         */
        fun getTimerQueue(guild: Guild, channel: GuildMessageChannel, number: Timer.Number): TimerQueue {
            return timerQueues.getOrPut(channel.idLong) { mutableMapOf() }
                .getOrPut(number) { TimerQueue(guild, channel, number) }
        }
    }

    //キュー
    private val timerQueue = LinkedList<Int>()

    /**
     * キューを追加する
     *
     * @param seconds 秒
     */
    fun addTimerQueue(seconds: Int) {
        //チャンネルのタイマーを取得する
        val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

        //タイマーが動いているかを確認
        if (channelTimers.containsKey(number)) {
            //キューに追加
            timerQueue.add(seconds)
        } else {
            //タイマーを開始し、インスタンスを代入する
            channelTimers[number] = Timer(channel, number, seconds, guild)
            Timer.channelsTimersMap[channel] = channelTimers
        }
    }

    /**
     * キューを空にする
     *
     */
    fun clearQueue() {
        timerQueue.clear()
    }

    /**
     * Indexからキューを削除
     *
     * @param index インデックス
     */
    fun removeQueueIndex(index: Int) {
        timerQueue.removeAt(index)
    }

    /**
     * キューを取得
     *
     * @return [LinkedList]<[Int]>
     */
    fun getQueue(): LinkedList<Int> {
        return LinkedList<Int>(timerQueue)
    }

    override fun onFinish(check: Boolean) {
        //キューがあるかを確認
        if (timerQueue.isEmpty()) return

        //チャンネルのタイマーを取得する
        val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }
        //タイマーを開始し、インスタンスを代入する
        channelTimers[number] = Timer(channel, number, timerQueue[0], guild)
        Timer.channelsTimersMap[channel] = channelTimers

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