package net.necromagic.simpletimerKT

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.necromagic.simpletimerKT.util.Log
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * 各タイマーのインスタンス用クラス
 *
 * @property channel [TextChannel] タイマーを動かすテキストチャンネル
 * @property number [Number] タイマーの番号
 * @property seconds [Int] 分数
 */
class Timer(val channel: TextChannel, val number: Number, private var seconds: Int) {
    companion object {
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

    //表示用 分・秒
    private var hun = seconds
    private var byo = 0

    //開始時の時間
    private var startNanoByo = System.nanoTime()

    private var beforeTimingNanoByo = System.nanoTime()

    //一時停止・終了フラグ
    private var stop = false
    private var finish = false

    //Displayのメッセージの文字列
    private var base = "タイマーを開始しました %s"

    //Displayと通知
    private var display: Message? = null
    var notice: Message? = null

    //終了時に呼び出されるクラス
    var finishListener: FinishListener? = null

    //強制的に更新を行うかのフラグ
    private var update = false

    //開始の処理へ飛ばす
    init {
        init()
    }

    /**
     * 開始
     *
     */
    private fun init() {
        sendDisplayMessage("${seconds}分00秒")
        Executors.newSingleThreadExecutor().submit {
            var i = 0
            while (i < seconds * 60) {
                //途中通知の確認
                if (i != 0 && i % 60 == 0) {
                    val time = seconds - i / 60
                    if (time % 10 == 0 || time == 5 || time == 3 || time == 2 || time == 1) {
                        sendMessage("のこり${time}分です%mention%")
                        sendTTS("のこり${time}分です", ServerConfig.TTSTiming.LV2)
                    }
                }

                //１秒待つ
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    Log.sendLog(e.stackTraceToString())
                }

                //終了確認
                if (finish) return@submit

                //時間の更新
                val oldI = i

                //停止してる時は時間を調整する
                if (stop) {
                    startNanoByo += System.nanoTime() - beforeTimingNanoByo
                } else {
                    i = ((System.nanoTime() - startNanoByo) / 1000000000L).toInt()
                }

                beforeTimingNanoByo = System.nanoTime()

                if (oldI == i) continue

                var hun: Int = seconds - i / 60
                var byo: Int = 60 - (i % 60)
                if (hun < 0) hun = 0
                if (byo != 60) hun -= 1
                if (byo == 60) byo = 0
                this.byo = byo
                this.hun = hun
                var byoString = byo.toString()
                if (byo < 10) byoString = "0$byoString"

                //10秒の倍数と残り5秒の時、updateのフラグが立っているときはdisplayを更新する
                if (byo % 10 == 0 || seconds * 60 - i == 5 || update) {
                    update = false
                    val action = display?.editMessage(number.format(base.format("${hun}分${byoString}秒")))
                    action?.queue({}, {})
                }
            }
            //終了
            finish()
        }
    }

    /**
     * 時間表示付きメッセージを送信
     *
     * @param data [String] メッセージ
     */
    private fun sendDisplayMessage(data: String) {
        try {
            //過去のメッセージを確認・削除
            if (display != null) {
                val id = display?.idLong
                timers.remove(id)
                displays.remove(id)
                display?.delete()?.complete()
            }
            //送信
            val display = channel.sendMessage(number.format(base.format(data))).complete()
            this.display = display
            //登録
            val id = display.idLong
            timers[id] = this
            displays[id] = this
            //リアクションの追加
            display.addReaction("U+25C0").queue({}, {})
            display.addReaction("U+23F8").queue({}, {})
            display.addReaction("U+1F6D1").queue({}, {})
            display.addReaction("1️⃣").queue({}, {})
            display.addReaction("3️⃣").queue({}, {})
            display.addReaction("5️⃣").queue({}, {})
            display.addReaction("\uD83D\uDD1F").queue({}, {})
        } catch (e: Exception) {
            Log.sendLog(e.stackTraceToString())
        }
    }

    /**
     * メッセージを送信
     *
     * @param string [String] メッセージ
     */
    private fun sendMessage(string: String) {
        try {
            //過去のメッセージを確認・削除
            if (notice != null) {
                timers.remove(notice?.idLong)
                notice?.delete()?.complete()
            }
        } catch (e: Exception) {
            Log.sendLog(e.stackTraceToString())
        } finally {
            //メッセージのメンションを書き換える
            val mention = SimpleTimer.instance.config.getMention(channel.guild)
            val message = "${number.format(string.replace("%mention%", ""))}${
                if (string.contains("%mention%")) {
                    when (mention) {
                        ServerConfig.Mention.NONE -> {
                            ""
                        }
                        ServerConfig.Mention.HERE -> {
                            "@here"
                        }
                        ServerConfig.Mention.VC -> {
                            val stringBuffer = StringBuffer()
                            val guild = channel.guild
                            for (voiceChannel in guild.voiceChannels) {
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
                val notice = display?.reply(message)?.mentionRepliedUser(false)?.complete()
                    ?: channel.sendMessage(message).complete()
                this.notice = notice
                timers[notice.idLong] = this
            } catch (e: Exception) {
                Log.sendLog(e.stackTraceToString())
            }
        }
    }

    /**
     * TTSメッセージを送信
     *
     * @param sting [String] メッセージ
     * @param timing [ServerConfig.TTSTiming] このメッセージのタイミング
     */
    private fun sendTTS(sting: String, timing: ServerConfig.TTSTiming) {
        //サーバーの設定の確認
        val config = SimpleTimer.instance.config
        if (config.checkTTS(channel.guild, timing)) {

            //メッセージを作成
            val messageBuilder = MessageBuilder("、${sting.replace("%mention%", "")}")
            messageBuilder.setTTS(true)

            try {
                //メッセージを送信
                val message = channel.sendMessage(messageBuilder.build()).complete()

                //時間を置いてメッセージを削除
                Executors.newSingleThreadExecutor().submit {
                    Thread.sleep(5000)
                    message.delete().queue({}, {})
                }
            } catch (e: Exception) {
                Log.sendLog(e.stackTraceToString())
            }

        }
    }

    /**
     * タイマーを延長
     *
     * @param i [Int] 延長する分
     */
    fun addTimer(i: Int) {
        //分数に加える
        seconds += i
        //延長と短縮を判定
        if (i >= 0) {
            sendMessage("タイマーを${i}分延長しました")
            sendTTS("タイマーを${i}分延長しました", ServerConfig.TTSTiming.LV3)
        } else {
            sendMessage("タイマーを${abs(i)}分短縮しました")
            sendTTS("タイマーを${abs(i)}分短縮しました", ServerConfig.TTSTiming.LV3)
        }
        //強制的に更新させる
        update = true
    }

    /**
     * タイマーを再開
     *
     */
    fun restart() {
        //停止の確認
        if (!stop) {
            sendMessage("タイマーは一時停止していません")
            return
        }
        //フラグを変更
        stop = false
        //新しいdisplayとして作る
        base = "タイマーを再開しました　%s"
        var byoString = byo.toString()
        if (byo < 10) byoString = "0$byoString"
        sendDisplayMessage("${hun}分${byoString}秒")
        sendTTS("タイマーを再開しました", ServerConfig.TTSTiming.LV3)
        notice?.clearReactions()?.queue({}, {})
    }

    /**
     * タイマーを一時停止
     *
     */
    fun stop() {
        //停止の確認
        if (stop) {
            sendMessage("タイマーは既に一時停止しています")
            return
        }
        //フラグを変更
        stop = true
        update = true
        //メッセージの送信とリアクション
        sendMessage("タイマーを一時停止しました")
        sendTTS("タイマーを一時停止しました", ServerConfig.TTSTiming.LV3)
        notice?.addReaction("U+25C0")?.queue({}, {})
    }

    /**
     * タイマーの終了
     *
     */
    fun finish() {
        //終了確認
        if (finish) {
            return
        }
        finish = true
        //登録の解除
        timers.remove(display?.idLong)
        displays.remove(display?.idLong)
        //メッセージを送信
        sendMessage("タイマーが終了しました%mention%")
        sendTTS(
            SimpleTimer.instance.config.getTTS(channel.guild).replace("x", number.number.toString()),
            ServerConfig.TTSTiming.LV1
        )
        //リスナーへの終了通知
        finishListener?.finish(this)
        timers.remove(notice?.idLong)
        //時間を置いてリアクションを削除
        Executors.newSingleThreadExecutor().submit {
            try {
                Thread.sleep(5000)
                display?.clearReactions()?.queue({}, {})
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
        var byoString = byo.toString()
        if (byo < 10) byoString = "0$byoString"
        sendDisplayMessage("${hun}分${byoString}秒")
    }

    /**
     * タイマーを破棄
     *
     */
    fun end() {
        //フラグを立てる
        finish = true
        //登録を消す
        timers.remove(display?.idLong)
        displays.remove(display?.idLong)
        sendMessage("タイマーを破棄しました")
        finishListener?.finish(this)
        timers.remove(notice?.idLong)
        //メッセージを消す
        Executors.newSingleThreadExecutor().submit {
            try {
                Thread.sleep(5000)
                display?.clearReactions()?.queue({}, {})
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }


    //タイマーの装飾
    enum class Number(private val string: String, val number: Int) {
        FIRST("```kotlin\n#1 %s\n```", 1),
        SECOND("```fix\n#2 %s\n```", 2),
        THIRD("```md\n#3 %s\n```", 3),
        FOURTH("```cs\n#4 %s\n```", 4);

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

    /**
     * タイマーの終了時処理用のインタフェース
     */
    interface FinishListener {
        /**
         * 終了時に実行する処理
         *
         * @param timer [Timer] 終了したタイマー
         */
        fun finish(timer: Timer)
    }
}