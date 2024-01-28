package dev.simpletimer.timer

import dev.simpletimer.component.button.AddTimerButton
import dev.simpletimer.component.button.DeleteMessageButton
import dev.simpletimer.component.button.FinishButton
import dev.simpletimer.component.button.StopButton
import dev.simpletimer.data.enum.Mention
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.database.data.TimerData
import dev.simpletimer.database.data.TimerMessageData
import dev.simpletimer.database.transaction.TimerDataTransaction
import dev.simpletimer.database.transaction.TimerMessageTransaction
import dev.simpletimer.extension.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * 各タイマーのインスタンス用クラス
 *
 */
class Timer(val timerData: TimerData) : TimerService.TimerListener {
    constructor(channel: GuildMessageChannel, number: Number, seconds: Int) : this(
        TimerDataTransaction.createTimerData(
            channel,
            number,
            seconds
        )
    )

    companion object {
        private val timerDataIdInstanceMap = ConcurrentHashMap<Long, Timer>()

        fun getTimerFromTimerDataId(timerDataId: Long): Timer? {
            return timerDataIdInstanceMap[timerDataId]
        }

        fun getUsableNumber(channel: GuildMessageChannel): List<Number> {
            val timerDataList = TimerDataTransaction.getTimerData(channel)
            return Number.entries.filterNot { entry -> timerDataList.map { it.number }.contains(entry) }
        }
    }

    //言語のデータ
    private val langData = timerData.channel.guild.getLang()

    //タイマーのコルーチンなどを行っている
    private val timerService = TimerService(timerData.seconds, timerData.timerServiceData)

    //Displayを更新するか
    private var forceUpdateDisplay = false

    //Displayの表示のベース
    private var base: String? = null
        set(value) {
            //DBを更新
            timerData.displayMessageBase = base
            TimerDataTransaction.updateTimerData(timerData)

            field = value
        }
        get() {
            //nullの時はデフォルト(開始時のメッセージ)を返すように
            return if (field == null) {
                langData.timer.start
            } else {
                field
            }
        }

    //Displayのメッセージ
    private var displayMessage: Message? = null
        set(value) {
            //もともとメッセージがある場合は削除をする
            if (field != null) {
                TimerMessageTransaction.removeTimerMessageData(field!!.idLong)
                field!!.delete().queue()
            }
            //DBに挿入・更新を行う
            if (value != null) {
                TimerMessageTransaction.upsertTimerMessageData(
                    timerData.timerDataId,
                    TimerMessageData(value.idLong, TimerMessageData.MessageType.DISPLAY)
                )

            }

            field = value
        }

    //通知のメッセージ
    private var noticeMessage: Message? = null
        set(value) {
            //もともとメッセージがある場合は削除する
            if (field != null) {
                TimerMessageTransaction.removeTimerMessageData(field!!.idLong)
                field!!.delete().queue()
            }
            //DBに挿入・更新を行う
            if (value != null) {
                TimerMessageTransaction.upsertTimerMessageData(
                    timerData.timerDataId,
                    TimerMessageData(value.idLong, TimerMessageData.MessageType.NORMAL)
                )
            }

            field = value
        }

    init {
        //初期化処理用のメソッドに任せる
        init()
    }

    /**
     * 初期化処理
     *
     */
    fun init() {
        //staticなmapに追加
        timerDataIdInstanceMap[timerData.timerDataId] = this

        //タイマーサービスのリスナーに追加
        timerService.registerListener(this)
        //キュー用のリスナーも追加
        timerService.registerListener(TimerQueue.getTimerQueue(timerData.channel, timerData.number))

        //DBに記録されているメッセージのデータを取得
        TimerMessageTransaction.getTimerMessagesFromTimerData(timerData).forEach { timeMessageData ->
            //メッセージをDiscordから取得
            timerData.channel.retrieveMessageById(timeMessageData.messageId).queue { message ->
                //種類を確認
                when (timeMessageData.messageType) {
                    //通知のメッセージ
                    TimerMessageData.MessageType.NORMAL -> {
                        noticeMessage = message
                        forceUpdateDisplay = true
                    }

                    //Displayのメッセージ
                    TimerMessageData.MessageType.DISPLAY -> {
                        displayMessage = message
                        forceUpdateDisplay = true
                    }
                }
            }
        }
    }

    /**
     * 開始
     *
     */
    fun start() {
        timerService.start()
    }

    /**
     * 終了
     *
     */
    fun finish() {
        timerService.finish()
    }

    /**
     * 一時停止
     *
     */
    fun stop() {
        timerService.stop()
    }

    /**
     * 再開
     *
     */
    fun restart() {
        timerService.restart()
    }

    /**
     * タイマーのDisplayを再表示
     *
     */
    fun check() {
        //新しいdisplayとして作る
        base = langData.timer.leftCheck
        sendDisplayMessage()
    }

    /**
     * 延長
     *
     * @param seconds 秒数
     */
    fun add(seconds: Int) {
        timerService.addTimer(seconds)
    }

    /**
     * 強制終了
     *
     */
    fun end() {
        timerService.end()
    }

    override fun onStart() {
        sendDisplayMessage()

        //データベースにて更新
        TimerDataTransaction.updateTimerData(timerData)
    }

    override fun onStop(check: Boolean) {
        //すでに止まっているかを確認
        if (!check) {
            sendNoticeMessage(langData.timer.alreadyStop, NoticeTiming.NONE)
            return
        }

        //Displayを更新
        displayMessage?.editMessageEmbeds(generateDisplayEmbed())?.queue()
        //通知系を送信
        sendNoticeMessage(langData.timer.stop, NoticeTiming.LV3)
        sendTTS(langData.timer.stop, NoticeTiming.LV3)

        //データベースにて更新
        TimerDataTransaction.updateTimerData(timerData)
    }

    override fun onRestart(check: Boolean) {
        //動いているかを
        if (!check) {
            sendNoticeMessage(langData.timer.notStop, NoticeTiming.NONE)
            return
        }

        //Displayのメッセージを変更
        base = langData.timer.restart
        //Displayを送信
        sendDisplayMessage()

        //ttsを送信
        sendTTS(langData.timer.restartTTS, NoticeTiming.LV3)

        //データベースにて更新
        TimerDataTransaction.updateTimerData(timerData)
    }

    override fun onFinish(check: Boolean) {
        //すでに終了している場合は終わり
        if (!check) return

        val guildData = timerData.channel.guild.getGuildData()

        //通知を送信
        sendNoticeMessage(langData.timer.finish, NoticeTiming.LV1, true)
        sendTTS(guildData.finishTTS.replace("x", timerData.number.number.toString()), NoticeTiming.LV1)

        //破棄の処理を行う
        destruction()
    }

    override fun onEnd(check: Boolean) {
        //すでに終了している場合は終わり
        if (!check) return

        //通知を送信
        sendNoticeMessage(langData.timer.remove, NoticeTiming.NONE, true)

        //破棄の処理を行う
        destruction()
    }

    override fun onAdd(seconds: Int) {
        //延長・短縮に応じてメッセージを作成
        val message = if (seconds >= 0) {
            //1分以上の延長
            if (seconds >= 60) {
                langData.timer.add.langFormatTime(langData, seconds / 60, seconds % 60)
            }
            //秒単位の延長
            else {
                langData.timer.add.langFormat(langData.timer.seconds.langFormat(seconds))
            }
        } else {
            //マイナスを絶対値値に
            val absSeconds = abs(seconds)
            //1分以上の短縮
            if (absSeconds >= 60) {
                langData.timer.minus.langFormatTime(langData, seconds / 60, seconds % 60)
            }
            //1分以下の短縮
            else {
                langData.timer.minus.langFormat(langData.timer.seconds.langFormat(seconds))
            }
        }

        //メッセージを送信
        sendNoticeMessage(message, NoticeTiming.LV3)
        sendTTS(message, NoticeTiming.LV3)

        //強制的に更新させるように
        forceUpdateDisplay = true

        //データベースにて更新
        TimerDataTransaction.updateTimerData(timerData)
    }

    //前回のupdateした時の時間を保管
    private var beforeTime: TimerService.Time = timerService.getTime()
    override fun onUpdate() {
        //権限の確認
        if (!timerData.channel.checkSimpleTimerPermission()) {
            forceUpdateDisplay = true
            return
        }

        //時間を取得
        val time = timerService.getTime()

        //時間が経過していない場合はなにもしない
        if (time == beforeTime) return

        beforeTime = time

        //通知タイミングの確認して送信
        if (time.seconds == 0 && (time.minute % 10 == 0 || time.minute == 5 || time.minute == 3 || time.minute == 2 || time.minute == 1)) {
            sendNoticeMessage(
                langData.timer.minutesLeftNotice.langFormat(langData.timer.minutes.langFormat(time.minute)),
                NoticeTiming.LV2
            )
            sendTTS(
                langData.timer.minutesLeftNotice.langFormat(langData.timer.minutes.langFormat(time.minute)),
                NoticeTiming.LV2
            )
        }

        //メッセージの更新タイミングの確認
        if (time.seconds % 10 == 0 || forceUpdateDisplay || (time.minute == 0 && time.seconds == 5)) {
            //強制更新のフラグを折る
            forceUpdateDisplay = false

            //Displayを更新
            displayMessage?.editMessageEmbeds(generateDisplayEmbed())?.queue()
        }
    }

    /**
     * タイマー表示メッセージを設置する
     *
     */
    private fun sendDisplayMessage() {
        //権限の確認
        if (!timerData.channel.checkSimpleTimerPermission()) return

        //埋め込みを設定後送信
        timerData.channel.sendMessageEmbeds(generateDisplayEmbed())
            //コンポーネントを設定
            .setComponents(
                ActionRow.of(
                    StopButton.createButton(timerData.number, timerData.channel.guild.getLang()),
                    FinishButton.createButton(timerData.number, timerData.channel.guild.getLang()),
                    AddTimerButton.createButton(timerData.number, timerData.channel.guild.getLang())
                )
            )
            //送信
            .queue { displayMessage ->
                this.displayMessage = displayMessage
                //リアクションをつける
                displayMessage.addReaction(Emoji.fromUnicode("1️⃣")).queue()
                displayMessage.addReaction(Emoji.fromUnicode("3️⃣")).queue()
                displayMessage.addReaction(Emoji.fromUnicode("5️⃣")).queue()
                displayMessage.addReaction(Emoji.fromUnicode("\uD83D\uDD1F")).queue()
            }
    }

    /**
     * Display用の[MessageEmbed]を作成する
     *
     * @return [MessageEmbed]
     */
    private fun generateDisplayEmbed(): MessageEmbed = EmbedBuilder().apply {
        val time = timerService.getTime()
        setDescription(timerData.number.format(base!!.langFormatTime(langData, time.minute, time.seconds)))
        //色を適用
        setColor(timerData.number.color)
    }.build()

    /**
     * 汎用メッセージを送信する
     *
     * @param message メッセージの本体
     * @param timing 送信タイミング
     * @param putDeleteButton 削除ボタンを設置するか
     */
    private fun sendNoticeMessage(message: String, timing: NoticeTiming, putDeleteButton: Boolean = false) {
        //権限を確認
        if (!timerData.channel.checkSimpleTimerPermission()) return

        //ギルドのデータ
        val guild = timerData.channel.guild
        val guildData = guild.getGuildData()

        //メンションを作成
        val mention = if (guildData.mentionTiming.priority >= timing.priority) {
            when (guildData.mention) {
                //メンションなし
                Mention.NONE -> {
                    ""
                }

                //@here
                Mention.HERE -> {
                    "@here"
                }

                //ロールに対してメンション
                Mention.ROLE -> {
                    //メンションを作成
                    guildData.roleMentionTargets.filterNotNull().joinToString { it.asMention }
                }

                //特定のVCにいる人にメンション
                Mention.TARGET_VC -> {
                    //登録しているチャンネルをすべて取得して結合
                    guildData.vcMentionTargets.filterNotNull().joinToString(" ") { audioChannel ->
                        //チャンネル内のメンバーをすべて取得して結合
                        audioChannel.members.joinToString(" ") { member ->
                            //メンションの形に
                            "<@${member.idLong}>"
                        }
                    }
                }

                //すべてのVCにいる人にメンション
                Mention.VC -> {
                    //すべてのVoiceChannelを取得して結合
                    guild.voiceChannels.joinToString(" ") { voiceChannel ->
                        //チャンネル内にいるメンバーをすべて取得して結合
                        voiceChannel.members.joinToString(" ") { member ->
                            //メンションの形に
                            "<@${member.idLong}>"
                        }
                    }
                }
            }
        } else {
            ""
        }

        //実際に送信するメッセージを作成
        val messageContext = "${timerData.number.format(message)}${mention}".let {
            if (it.length >= 2000) {
                "${timerData.number.format(message)}${langData.timer.mentionOmit}"
            }else {
                it
            }
        }

        //返信をまずは試みる
        displayMessage?.reply("${timerData.number.format(message)}${mention}")?.apply {
            //削除のボタンを設置
            if (putDeleteButton) setComponents(ActionRow.of(DeleteMessageButton.createButton(0, guild.getLang())))
        }
            ?.mentionRepliedUser(false)
            ?.queue {
                noticeMessage = it
            }
        //返信でできなかったときは通常どおりのメッセージを送信
            ?: {
                timerData.channel.sendMessage(messageContext).apply {
                    //削除のボタンを設置
                    if (putDeleteButton) setComponents(
                        ActionRow.of(
                            DeleteMessageButton.createButton(
                                0,
                                guild.getLang()
                            )
                        )
                    )
                }.queue {
                    noticeMessage = it
                }
            }
    }

    /**
     * TTSによる呼び上げメッセージを送信する
     *
     * @param message メッセージの内容
     * @param timing 送信タイミング
     */
    private fun sendTTS(message: String, timing: NoticeTiming) {
        if (!timerData.channel.checkSimpleTimerPermission()) return

        //何もないときは読み上げない
        if (message == "") return

        //ギルドのデータ
        val guildData = timerData.channel.guild.getGuildData()

        if (guildData.ttsTiming.priority < timing.priority) return

        //メッセージを作成
        MessageCreateBuilder().setContent("、$message").apply {
            //ttsを作成
            setTTS(true)
        }.let {
            //送信
            timerData.channel.sendMessage(it.build()).queue { message ->
                //時間を置いてメッセージを削除
                CoroutineScope(Dispatchers.Default).launch {
                    delay(5000)
                    message.delete().queue()
                }
            }
        }
    }

    /**
     * タイマーを破棄する
     *
     */
    private fun destruction() {
        //ゴミ箱のボタンを設置
        displayMessage?.editMessageEmbeds(generateDisplayEmbed())?.setComponents(
            ActionRow.of(
                DeleteMessageButton.createButton(0, timerData.channel.guild.getLang())
            )
        )?.queue()

        //つけているリアクションを削除
        CoroutineScope(Dispatchers.Default).launch {
            delay(5000)
            //リアクションを削除
            displayMessage?.clearReactions()?.queue()

            //各種メッセージを削除
            displayMessage?.let {
                //DBから削除
                TimerMessageTransaction.removeTimerMessageData(displayMessage!!.idLong)
                //削除
                displayMessage!!.delete().queue()
            }
            noticeMessage?.let {
                //DBから削除
                TimerMessageTransaction.removeTimerMessageData(noticeMessage!!.idLong)
                //削除
                noticeMessage!!.delete().queue()
            }
            //タイマーのデータをDBから削除
            TimerDataTransaction.deleteTimerData(timerData)

            //マップから削除
            timerDataIdInstanceMap.remove(timerData.timerDataId)
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