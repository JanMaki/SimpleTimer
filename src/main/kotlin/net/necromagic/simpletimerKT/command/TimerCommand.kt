package net.necromagic.simpletimerKT.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.necromagic.simpletimerKT.*
import net.necromagic.simpletimerKT.Timer
import net.necromagic.simpletimerKT.util.MessageReply
import net.necromagic.simpletimerKT.util.SendMessage
import net.necromagic.simpletimerKT.util.equalsIgnoreCase
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * タイマーのコマンドのクラス
 */
class TimerCommand : CommandData("timer", "タイマーを開始します。タイマーの操作は!!timerコマンドを使用してください。"), RunCommand,
    Timer.FinishListener {

    init {
        setDefaultEnabled(true)

        addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で").setRequired(true))
    }

    //チャンネルとタイマーのマップ
    private val channelsTimersMap = HashMap<TextChannel, EnumMap<Timer.Number, Timer>>()

    /**
     * コマンドを実行する
     * @param user [User] 実行したユーザー
     * @param channel [TextChannel] 実行したチャンネル
     * @param args [List] 内容
     * @param messageReply [MessageReply] 返信を行うクラス。
     */
    override fun runCommand(user: User, channel: TextChannel, args: List<String>, messageReply: MessageReply) {
        val prefix = SimpleTimer.instance.config.getPrefix(channel.guild)

        //labelの確認・ヘルプの表示
        if (args.size < 2) {
            try {
                channel.sendMessage(createHelpEmbedBuilder(prefix)).queue({}, {})
            } catch (e: InsufficientPermissionException) {
                SendMessage.sendErrorMessageToUser(user)
            }
            return
        }
        val label = args[1]


        val channelTimers = channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

        //コマンドの実装
        if (label.equalsIgnoreCase("add")) {
            if (args.size < 4) {
                SendMessage.sendMessage(channel, "${prefix}timer add #1|#2|#3|#4 分", user)
                return
            }
            try {
                val i = Integer.parseInt(args[2].replace("#", ""))
                if (i > 4 || i < 1) {
                    SendMessage.sendMessage(channel, "${prefix}timer add #1|#2|#3|#4 秒", user)
                    return
                }
                val number = Timer.Number.getNumber(i)
                if (!channelTimers.containsKey(number)) {
                    if (number != null)
                        SendMessage.sendMessage(channel, number.format("タイマーは動いていません"), user)
                    return
                }
                val time = Integer.parseInt(args[3])
                val timer = channelTimers[number]!!
                timer.addTimer(time)
            } catch (e: Exception) {
                SendMessage.sendMessage(channel, "${prefix}timer add #1|#2|#3|#4 秒", user)
            }
        } else if (label.equalsIgnoreCase("check")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer check #1|#2|#3|#4", user)
                return
            }
            try {
                val i = Integer.parseInt(args[2].replace("#", ""))
                if (i > 4 || i < 1) {
                    SendMessage.sendMessage(channel, "${prefix}timer check #1|#2|#3|#4", user)
                    return
                }
                val number = Timer.Number.getNumber(i)
                if (!channelTimers.containsKey(number)) {
                    if (number != null)
                        SendMessage.sendMessage(channel, number.format("タイマーは動いていません"), user)
                    return
                }
                val timer = channelTimers[number]!!
                timer.check()
            } catch (e: java.lang.Exception) {
                SendMessage.sendMessage(channel, "${prefix}timer check #1|#2|#3|#4", user)
            }
        } else if (label.equalsIgnoreCase("finish") || label.equalsIgnoreCase("fin")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer fin #1|#2|#3|#4", user)
                return
            }
            try {
                val i = Integer.parseInt(args[2].replace("#", ""))
                if (i > 4 || i < 1) {
                    SendMessage.sendMessage(channel, "${prefix}timer fin #1|#2|#3|#4", user)
                    return
                }
                val number = Timer.Number.getNumber(i)
                if (!channelTimers.containsKey(number)) {
                    if (number != null)
                        SendMessage.sendMessage(channel, number.format("タイマーは動いていません"), user)
                    return
                }
                val timer = channelTimers[number]!!
                timer.finish()
            } catch (e: Exception) {
                SendMessage.sendMessage(channel, "${prefix}timer fin #1|#2|#3|#4", user)
            }
        } else if (label.equalsIgnoreCase("finishAll") || label.equalsIgnoreCase("finAll")) {
            if (channelTimers.keys.size == 0) {
                SendMessage.sendMessage(channel, "タイマーは動いていません", user)
            }
            val timers = HashSet<Timer>()
            for (number in Timer.Number.values()) {
                val timer = channelTimers[number]
                if (timer != null)
                    timers.add(timer)
            }
            for (timer in timers) {
                timer.finish()
            }
        } else if (label.equalsIgnoreCase("stop")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer stop #1|#2|#3|#4", user)
                return
            }
            try {
                val i = Integer.parseInt(args[2].replace("#", ""))
                if (i > 4 || i < 1) {
                    SendMessage.sendMessage(channel, "${prefix}timer stop #1|#2|#3|#4", user)
                    return
                }
                val number = Timer.Number.getNumber(i)
                if (!channelTimers.containsKey(number)) {
                    if (number != null)
                        SendMessage.sendMessage(channel, number.format("タイマーは動いていません"), user)
                    return
                }
                val timer = channelTimers[number]!!
                timer.stop()
            } catch (e: Exception) {
                SendMessage.sendMessage(channel, "${prefix}timer stop #1|#2|#3|#4", user)
            }
        } else if (label.equalsIgnoreCase("restart")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer restart #1|#2|#3|#4", user)
                return
            }
            try {
                val i = Integer.parseInt(args[2].replace("#", ""))
                if (i > 4 || i < 1) {
                    SendMessage.sendMessage(channel, "${prefix}timer restart #1|#2|#3|#4", user)
                    return
                }
                val number = Timer.Number.getNumber(i)
                if (!channelTimers.containsKey(number)) {
                    if (number != null)
                        SendMessage.sendMessage(channel, number.format("タイマーは動いていません"), user)
                    return
                }
                val timer = channelTimers[number]!!
                timer.restart()
            } catch (e: Exception) {
                SendMessage.sendMessage(channel, "${prefix}timer restart #1|#2|#3|#4", user)
            }
        } else if (label.equalsIgnoreCase("finishtts")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer finishtts <Message>", user)
                return
            }
            val mes = args[2]
            if (mes.length > 40) {
                SendMessage.sendMessage(channel, "終了時のTTSメッセージは50文字以下にしてください", user)
            }
            SimpleTimer.instance.config.setFinishTTS(channel.guild, mes)
            SimpleTimer.instance.config.save()
            SendMessage.sendMessage(channel, "終了時のTTSメッセージを変更しました", user)
        } else if (label.equalsIgnoreCase("tts")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer tts lv0/lv1/lv2/lv3", user)
                return
            }
            val timing = when {
                args[2].equalsIgnoreCase("LV1") -> ServerConfig.TTSTiming.LV1
                args[2].equalsIgnoreCase("LV2") -> ServerConfig.TTSTiming.LV2
                args[2].equalsIgnoreCase("LV3") -> ServerConfig.TTSTiming.LV3
                else -> ServerConfig.TTSTiming.LV0
            }
            SimpleTimer.instance.config.setTTS(channel.guild, timing)
            SendMessage.sendMessage(channel, "チャットの読み上げを${timing}にしました", user)
            SimpleTimer.instance.config.save()
        } else if (label.equalsIgnoreCase("mention")) {
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "${prefix}timer mention off/here/vc", user)
                return
            }
            val mention = when (args[2]) {
                "off" -> ServerConfig.Mention.NONE
                "here" -> ServerConfig.Mention.HERE
                "vc" -> ServerConfig.Mention.VC
                else -> {
                    SendMessage.sendMessage(channel, "${prefix}timer mention off/here/vc", user)
                    return
                }
            }
            SimpleTimer.instance.config.setMention(channel.guild, mention)
            SendMessage.sendMessage(channel, "メンションの設定を${mention}にしました", user)
            SimpleTimer.instance.config.save()
        } else if (label.equalsIgnoreCase("prefix")) {
            val newPrefix = if (args.size < 3) {
                "!"
            } else {
                args[2]
            }
            if (!prefix.equalsIgnoreCase("!") && prefix.equalsIgnoreCase(newPrefix)) {
                SendMessage.sendMessage(channel, "すでにPrefixは'${newPrefix}'になっています", user)
                return
            }
            if (newPrefix.length > 5) {
                SendMessage.sendMessage(channel, "Prefixは５文字以下にしてください", user)
                return
            }
            SimpleTimer.instance.config.setPrefix(channel.guild, newPrefix)
            if (args.size < 3) {
                SendMessage.sendMessage(channel, "Prefixをリセットしました", user)
            } else {
                SendMessage.sendMessage(channel, "Prefixを変更しました $prefix -> $newPrefix", user)
            }
            SimpleTimer.instance.config.save()
        } else if (label.equalsIgnoreCase("count")) {
            SendMessage.sendMessage(channel, "${Timer.getCount()}個のタイマーが稼働しています", user)
        } else if (label.equalsIgnoreCase("log")) {
            SimpleTimer.instance.config.set("LoggingServer.${channel.guild.id}", channel.id)
            SimpleTimer.instance.config.save()
            channel.sendMessage("設定を行いました").queue({}, {})
        } else {
            val i: Int
            try {
                i = Integer.parseInt(label)
            } catch (e: Exception) {
                try {
                    channel.sendMessage(createHelpEmbedBuilder(prefix)).queue({}, {})
                } catch (e2: InsufficientPermissionException) {
                    error(user)
                }
                return
            }
            for (number in Timer.Number.values()) {
                if (!channelTimers.containsKey(number)) {
                    val timer = Timer(channel, number, i)
                    timer.finishListener = this
                    channelTimers[number] = timer
                    channelsTimersMap[channel] = channelTimers
                    return
                }
            }
            SendMessage.sendMessage(channel, ":x: これ以上タイマーを動かすことはできません（最大: 4）", user)
        }
    }


    //ヘルプの埋め込みの履歴をキャッシュとして残す
    private val helpEmbedMap = HashMap<String, MessageEmbed>()

    /**
     * ヘルプの埋め込みメッセージを作成する
     *
     * @param prefix [String] ヘルプ内のコマンドの頭の文字列
     * @return [MessageEmbed] ヘルプの埋め込みメッセージ
     */
    private fun createHelpEmbedBuilder(prefix: String): MessageEmbed {
        //過去の履歴を確認する
        val messageEmbed = helpEmbedMap[prefix]
        if (messageEmbed != null) {
            return messageEmbed
        }
        //新しく作成する
        val helpEmbedBuilder = EmbedBuilder()
        helpEmbedBuilder.setTitle("SimpleTimer")
        helpEmbedBuilder.setDescription(SimpleTimer.instance.version)
        helpEmbedBuilder.addField("${prefix}timer 分", "タイマーを開始します（最大4つまで）\n分: 時間を分単位で", false)
        helpEmbedBuilder.addField("${prefix}timer fin #1|#2|#3|#4", "タイマーを終了します\n#-: タイマーの番号", false)
        helpEmbedBuilder.addField("${prefix}timer finAll", "すべてのタイマーを終了します", false)
        helpEmbedBuilder.addField(
            "${prefix}timer add #1|#2|#3|#4 分",
            "タイマーを延長します\nマイナスの値で時間を短縮できます\n#-: タイマーの番号\n分: 時間を分単位で",
            false
        )
        helpEmbedBuilder.addField("${prefix}timer stop #1|#2|#3|#4", "タイマーを一時停止します\n#-: タイマーの番号", false)
        helpEmbedBuilder.addField("${prefix}timer restart #1|#2|#3|#4", "タイマーの一時停止を再開します\n#-: タイマーの番号", false)
        helpEmbedBuilder.addField("${prefix}timer check #1|#2|#3|#4", "タイマーの時間を確認します\n#-: タイマーの番号", false)
        helpEmbedBuilder.addField(
            "${prefix}timer tts lv0|lv1|lv2|lv3",
            "ttsによるメッセージの読み上げを設定します\nLV0: TTSによる通知を行わない\nLV1: タイマー終了時のみ通知\nLV2: タイマー終了時と、定期的な時間通知の時に通知\nLV3: タイマーのすべての通知（LV2に加えて、延長など）で通知\nttsメッセージを使用する権限が必要です\n初期状態ではLV0になっています",
            false
        )
        helpEmbedBuilder.addField(
            "${prefix}timer finishtts <Message>",
            "終了のメッセージ読み上げの内容を変更します\nタイマーの番号が\"x\"へ代入されます\n例: ${prefix}timer finishtts x番目のタイマーが終了しました",
            false
        )
        helpEmbedBuilder.addField(
            "${prefix}timer mention here|vc|off",
            "メンションを用いた通知の設定を変更します\nhere: hereを用いたメンション\nvc: ボイスチャットに接続しているメンバー\noff: メンションを行わない\n初期状態ではhereになっています",
            false
        )
        helpEmbedBuilder.addField(
            "${prefix}timer prefix [Prefix]",
            "コマンドの頭の文字を変更します\n 例: '${prefix}timer prefix ?' 次のコマンドからは、?timerとなります\n例2: '${prefix}timer prefix' 何も指定しないと、デフォルトである!timerになります",
            false
        )
        helpEmbedBuilder.addField("", "*コマンドがエラーや、他のbotとの競合などで使えない時は、**!!timer**で実行ができます。", false)
        val newMessageEmbed = helpEmbedBuilder.build()
        helpEmbedMap[prefix] = newMessageEmbed
        return newMessageEmbed
    }


    /**
     * Timer終了時の処理
     *
     * @param timer [Timer] 終了したTimer
     */
    override fun finish(timer: Timer) {
        val channelTimers = channelsTimersMap[timer.channel]
        if (channelTimers != null) {
            channelTimers.remove(timer.number)
            channelsTimersMap[timer.channel] = channelTimers
        }
    }
}