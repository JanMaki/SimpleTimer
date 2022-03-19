package dev.simpletimer.timer

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.button.AddTimerButton
import dev.simpletimer.component.button.DeleteMessageButton
import dev.simpletimer.data.enum.Mention
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.extension.getAudioPlayer
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.timer.Timer.Number
import dev.simpletimer.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.components.ActionRow
import java.awt.Color
import java.util.*
import kotlin.math.abs

/**
 * 各タイマーのインスタンス用クラス
 *
 * @property channel [MessageChannel] タイマーを動かすテキストチャンネル
 * @property number [Number] タイマーの番号
 * @property seconds [Int] 秒数
 */
class Timer(
    val channel: MessageChannel,
    private val number: Number,
    private var seconds: Int,
    private val guild: Guild
) : TimerService.TimerListener {
    companion object {
        //チャンネルとタイマーのマップ
        val channelsTimersMap = HashMap<MessageChannel, EnumMap<Number, Timer>>()

        private val displays = TreeMap<Long, Timer>()
        private val timers = TreeMap<Long, Timer>()

        /**
         * メッセージのIDからTimerを取得する
         *
         * @param id メッセージのID
         * @return 該当のTimer。null許容
         */
        fun getTimer(id: Long): Timer? {
            return timers[id]
        }

        /**
         * メッセージがTimerのDisplayか確認をする
         *
         * @param id メッセージのID
         * @return 結果 true->Displayである
         */
        fun isDisplay(id: Long): Boolean {
            return displays.containsKey(id)
        }

        /**
         * 動いてるタイマーの数を取得する
         *
         * @return 動いてるタイマーの数
         */
        fun getCount(): Int {
            return displays.size
        }
    }

    //Displayのメッセージの文字列
    private var base = "タイマーを開始しました %s"

    //Displayと通知
    private var display: Message? = null
    var notice: Message? = null

    //強制的に更新を行うかのフラグ
    private var update = false

    //タイマーのサービス
    private val timerService = TimerService(seconds)

    //開始の処理へ飛ばす
    init {
        init()
    }

    /**
     * 開始
     *
     */
    private fun init() {
        timerService.registerListener(this)

        timerService.registerListener(TimerQueue.getTimerQueue(guild, channel, number))

        timerService.start()
    }

    override fun onStart() {
        val time = timerService.getTime()
        sendDisplayMessage(time)
    }

    //以前のUpdate時の時間を保管する
    private var oldTime: TimerService.Time? = null

    override fun onUpdate() {
        //時間を取得
        val time = timerService.getTime()

        //最初の1秒はなにもしない
        if (time == TimerService.Time.getTimeFromTotalSeconds(seconds) && oldTime == null) {
            return
        }

        //以前の時間と同じかを確認
        if (time == oldTime) return
        //以前に時間を保管
        oldTime = time

        //途中通知の確認
        if (time.seconds == 0 && (time.minute % 10 == 0 || time.minute == 5 || time.minute == 3 || time.minute == 2 || time.minute == 1)) {
            sendMessage("のこり${time.minute}分です", NoticeTiming.LV2)
            sendTTS("のこり${time.minute}分です", NoticeTiming.LV2)
        }

        //10秒の倍数と残り5秒の時、updateのフラグが立っているときはdisplayを更新する
        if (time.seconds % 10 == 0 || update || (time.minute == 0 && time.seconds == 5)) {
            update = false
            display?.editMessageEmbeds(generateDisplayEmbed(time))?.queue()
        }
    }

    /**
     * タイマーを延長
     *
     * @param i [Int] 延長する秒
     */
    fun add(i: Int) {
        //分数に加える
        timerService.addTimer(i)
    }

    override fun onAdd(seconds: Int) {
        //延長と短縮を判定
        if (seconds >= 0) {
            if (seconds >= 60) {
                sendMessage("タイマーを${seconds / 60}分${seconds % 60}秒延長しました", NoticeTiming.LV3)
                sendTTS("タイマーを${seconds / 60}秒${seconds % 60}延長しました", NoticeTiming.LV3)
            } else {
                sendMessage("タイマーを${seconds}秒延長しました", NoticeTiming.LV3)
                sendTTS("タイマーを${seconds}秒延長しました", NoticeTiming.LV3)
            }
        } else {
            val absSeconds = abs(seconds)
            if (absSeconds >= 60) {
                sendMessage("タイマーを${absSeconds / 60}分${absSeconds % 60}秒短縮しました", NoticeTiming.LV3)
                sendTTS("タイマーを${absSeconds / 60}分${absSeconds % 60}秒短縮しました", NoticeTiming.LV3)
            } else {
                sendMessage("タイマーを${seconds}秒短縮しました", NoticeTiming.LV3)
                sendTTS("タイマーを${seconds}秒短縮しました", NoticeTiming.LV3)
            }
        }
        //強制的に更新させる
        update = true
    }

    /**
     * タイマーを再開
     *
     */
    fun restart() {
        timerService.restart()
    }

    override fun onRestart(check: Boolean) {
        if (!check) {
            sendMessage("タイマーは一時停止していません", NoticeTiming.NONE)
            return
        }
        //新しいdisplayとして作る
        base = "タイマーを再開しました　%s"
        val time = timerService.getTime()
        sendDisplayMessage(time)
        sendTTS("タイマーを再開しました", NoticeTiming.LV3)
        notice?.clearReactions()?.queue()
    }

    /**
     * タイマーを一時停止
     *
     */
    fun stop() {
        timerService.stop()
    }

    override fun onStop(check: Boolean) {
        //停止の確認
        if (!check) {
            sendMessage("タイマーは既に一時停止しています", NoticeTiming.NONE)
            return
        }
        update = true
        //メッセージの送信とリアクション
        sendMessage("タイマーを一時停止しました", NoticeTiming.LV3)
        sendTTS("タイマーを一時停止しました", NoticeTiming.LV3)
        notice?.addReaction("U+25C0")?.queue()
        //ディスプレイを更新
        val time = timerService.getTime()
        display?.editMessageEmbeds(generateDisplayEmbed(time))?.queue()
    }

    /**
     * タイマーの終了
     *
     */
    fun finish() {
        timerService.finish()
    }

    override fun onFinish(check: Boolean) {
        //終了確認
        if (!check) {
            return
        }
        //登録の解除
        timers.remove(display?.idLong)
        displays.remove(display?.idLong)
        //ギルドのデータを取得
        val guildData = guild.getGuildData()
        //メッセージを送信
        sendMessage("タイマーが終了しました", NoticeTiming.LV1, true)
        sendTTS(
            guildData.finishTTS.replace("x", number.number.toString()),
            NoticeTiming.LV1
        )

        //ディスプレイを更新
        val time = timerService.getTime()
        //削除のボタンをつける
        display?.editMessageEmbeds(generateDisplayEmbed(time))
            ?.setActionRows(ActionRow.of(DeleteMessageButton.createButton(0)))?.queue()

        //プレイヤーを取得
        val player = guild.getAudioPlayer()
        //オーディオを探す
        val audioDatum = SimpleTimer.instance.dataContainer.audioDatum.filter { it.id == guild.getGuildData().audio }
        //見つかったかを確認
        if (audioDatum.isNotEmpty() && player.isConnected()) {
            //再生
            player.play(audioDatum.first())
        }

        val channelTimers = channelsTimersMap[channel]
        if (channelTimers != null) {
            channelTimers.remove(number)
            channelsTimersMap[channel] = channelTimers
        }

        if (notice != null) timers.remove(notice?.idLong)

        //時間を置いてリアクションを削除
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(5000)
                display?.clearReactions()?.queue()
            } catch (e: InterruptedException) {
                Log.sendLog(e.stackTraceToString())
            }
        }
    }

    /**
     * 確認
     *
     */
    fun check() {
        //新しいdisplayとして作る
        base = "タイマー終了まで: %s"
        val time = timerService.getTime()
        sendDisplayMessage(time)
    }

    /**
     * タイマーを破棄
     *
     */
    fun end() {
        timerService.end()
    }

    override fun onEnd(check: Boolean) {
        if (!check) {
            return
        }
        //登録を消す
        timers.remove(display?.idLong)
        displays.remove(display?.idLong)
        sendMessage("タイマーを破棄しました", NoticeTiming.NONE, true)

        //ディスプレイを更新
        val time = timerService.getTime()
        //削除のボタンをつける
        display?.editMessageEmbeds(generateDisplayEmbed(time))
            ?.setActionRows(ActionRow.of(DeleteMessageButton.createButton(0)))?.queue()

        val channelTimers = channelsTimersMap[channel]
        if (channelTimers != null) {
            channelTimers.remove(number)
            channelsTimersMap[channel] = channelTimers
        }

        if (notice != null) timers.remove(notice?.idLong)
        //メッセージを消す
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(5000)
                display?.clearReactions()?.queue()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 時間表示付きメッセージを送信
     *
     * @param data [String] メッセージ
     */
    private fun sendDisplayMessage(time: TimerService.Time) {
        try {
            //過去のメッセージを確認・削除
            if (display != null) {
                val id = display?.idLong
                timers.remove(id)
                displays.remove(id)
                display?.delete()?.queue()
            }
            //送信
            channel.sendMessageEmbeds(generateDisplayEmbed(time))
                .setActionRow(AddTimerButton.createButton(number))
                .queue { display ->
                    this.display = display
                    //登録
                    val id = display.idLong
                    timers[id] = this
                    displays[id] = this
                    //リアクションの追加
                    display.addReaction("U+25C0").queue()
                    display.addReaction("U+23F8").queue()
                    display.addReaction("U+1F6D1").queue()
                    display.addReaction("1️⃣").queue()
                    display.addReaction("3️⃣").queue()
                    display.addReaction("5️⃣").queue()
                    display.addReaction("\uD83D\uDD1F").queue()
                }
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)) {
                return
            }
            Log.sendLog(e.stackTraceToString())
        }
    }


    /**
     * ディスプレイの埋め込みを作成する
     *
     * @param time 表示する時間[TimerService.Time]
     * @return 作成した[MessageEmbed]
     */
    private fun generateDisplayEmbed(time: TimerService.Time): MessageEmbed {
        //ビルダー
        val embed = EmbedBuilder()
        //時間を説明文に書く
        embed.setDescription(number.format(base.format("${time.minute}分${time.seconds}秒")))
        //色を設定
        embed.setColor(number.color)
        //作成して返す
        return embed.build()
    }


    /**
     * メッセージを送信
     *
     * @param string [String] メッセージ
     */
    private fun sendMessage(string: String, timing: NoticeTiming, deletable: Boolean = false) {
        try {
            //過去のメッセージを確認・削除
            if (notice != null) {
                timers.remove(notice?.idLong)
                notice?.delete()?.queue()
            }
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)) {
                return
            }
            Log.sendLog(e.stackTraceToString())
        } finally {
            //メッセージのメンションを書き換える
            val guildData = guild.getGuildData()
            val mention = guildData.mention
            val message = "${number.format(string)}${
                if (guildData.mentionTiming.priority >= timing.priority) {
                    when (mention) {
                        //何も書かない
                        Mention.NONE -> {
                            ""
                        }
                        //hereのメンション
                        Mention.HERE -> {
                            "@here"
                        }
                        //VCへのメンション
                        Mention.VC -> {
                            val stringBuffer = StringBuffer()
                            val guild = guild
                            //すべてのVCを確認
                            for (voiceChannel in guild.voiceChannels) {
                                //メンバーを追加する
                                for (member in voiceChannel.members) {
                                    if (member.user.isBot) {
                                        continue
                                    }
                                    stringBuffer.append("<@")
                                    stringBuffer.append(member.idLong)
                                    stringBuffer.append(">")
                                }
                            }
                            stringBuffer.toString()
                        }
                        //ロールへメンション
                        Mention.ROLE -> {
                            guildData.roleMentionTargets.filterNotNull().map { "<@&" + it.idLong + ">" }
                                .joinToString { "" }
                        }
                        //対象のVCへのメンション
                        Mention.TARGET_VC -> {
                            val stringBuffer = StringBuffer()
                            //対象のVCがあるかを確認
                            //すべてのVCを確認
                            for (voiceChannel in guildData.vcMentionTargets.filterNotNull()) {
                                //メンバーを追加する
                                for (member in voiceChannel.members) {
                                    if (member.user.isBot) {
                                        continue
                                    }
                                    stringBuffer.append("<@")
                                    stringBuffer.append(member.idLong)
                                    stringBuffer.append(">")
                                }
                            }
                            stringBuffer.toString()
                        }
                    }
                } else {
                    ""
                }
            }"

            try {
                //送信・登録
                display?.reply(message)
                    ?.apply {
                        //削除のボタンをつける
                        if (deletable) setActionRows(ActionRow.of(DeleteMessageButton.createButton(0)))
                    }
                    ?.mentionRepliedUser(false)?.queue { notice ->
                        timers[notice.idLong] = this
                        this.notice = notice
                    } ?: channel.sendMessage(message).queue { notice ->
                    timers[notice.idLong] = this
                    this.notice = notice
                }
            } catch (e: Exception) {
                Log.sendLog(e.stackTraceToString())
            }
        }
    }

    /**
     * TTSメッセージを送信
     *
     * @param sting [String] メッセージ
     * @param timing [NoticeTiming] このメッセージのタイミング
     */
    private fun sendTTS(sting: String, timing: NoticeTiming) {
        //何もないときは読み上げない
        if (sting.replace(" ", "").replace("　", "") == "") return

        //ギルドのデータの確認
        val guildData = guild.getGuildData()
        if (guildData.ttsTiming.priority >= timing.priority) {

            //メッセージを作成
            val messageBuilder = MessageBuilder("、${sting.replace("", "")}")
            messageBuilder.setTTS(true)

            try {
                //メッセージを送信
                channel.sendMessage(messageBuilder.build()).queue { message ->
                    //時間を置いてメッセージを削除
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(5000)
                        message.delete().queue()
                    }
                }
            } catch (e: Exception) {
                //権限関係が原因の物は排除
                if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)) {
                    return
                }
                Log.sendLog(e.stackTraceToString())
            }

        }
    }

    //タイマーの装飾
    enum class Number(private val string: String, val number: Int, val color: Color) {
        FIRST("```kotlin\n#1 %s\n```", 1, Color.WHITE),
        SECOND("```fix\n#2 %s\n```", 2, Color(183, 133, 43)),
        THIRD("```md\n#3 %s\n```", 3, Color(3, 144, 205)),
        FOURTH("```cs\n#4 %s\n```", 4, Color(205, 67, 39));

        /**
         * タイマーの装飾を適用する
         *
         * @param value [String] 適用する文字列
         * @return [String] 適用後の文字列
         */
        fun format(value: String): String {
            return string.format(value)
        }

        companion object {
            /**
             * 数値から、Numberを手に入れる
             *
             * @param i [Int] 対象の数値
             * @return [Number] 結果
             */
            fun getNumber(i: Int): Number? {
                when (i) {
                    1 -> {
                        return FIRST
                    }
                    2 -> {
                        return SECOND
                    }
                    3 -> {
                        return THIRD
                    }
                    4 -> {
                        return FOURTH
                    }
                }
                return null
            }
        }
    }
}