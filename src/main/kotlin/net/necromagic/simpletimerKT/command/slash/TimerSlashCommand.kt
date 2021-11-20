package net.necromagic.simpletimerKT.command.slash

import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.necromagic.simpletimerKT.ServerConfig
import net.necromagic.simpletimerKT.SimpleTimer
import net.necromagic.simpletimerKT.Timer
import net.necromagic.simpletimerKT.util.equalsIgnoreCase
import java.util.*

class TimerSlashCommand {

    /**
     * タイマーを開始する
     */
    object StartTimer : SlashCommand("timer", "タイマーを開始する") {
        init {
            setDefaultEnabled(true)

            addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("分")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }


            //秒数を取得
            val minutes = option.asLong

            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始
                    val timer = Timer(channel, number, minutes.toInt())

                    //タイマーのインスタンスを代入する
                    channelTimers[number] = timer
                    Timer.channelsTimersMap[channel] = channelTimers

                    //空白を出力して消し飛ばす
                    event.hook.sendMessage("|| ||").complete().delete().queue()
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）").queue()
        }
    }

    /**
     * タイマーを終了する
     */
    object Finish : SlashCommand("finish", "タイマーを終了する") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "終了するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("タイマー")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //タイマーの番号を取得
            val i = option.asLong.toInt()


            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                //タイマーが稼働していないことを教えるメッセージを出力
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue()
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを終わらせる
            timer.finish()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").complete().delete().queue()
        }
    }

    /**
     * すべてのタイマーを終了
     */
    object FinAll : SlashCommand("finish_all", "すべてのタイマーを終了する") {
        init {
            setDefaultEnabled(true)
        }

        override fun run(command: String, event: SlashCommandEvent) {

            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //稼働しているタイマーの数を取得
            if (channelTimers.keys.size == 0) {
                event.hook.sendMessage("タイマーは動いていません").queue()
                return
            }

            //タイマーをすべて終了させる
            for (number in Timer.Number.values()) {
                channelTimers[number]?.finish()
            }

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").complete().delete().queue()
        }
    }

    /**
     * タイマーを延長する
     */
    object Add : SlashCommand("add", "タイマーを延長する") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "延長するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                ),
                OptionData(OptionType.INTEGER, "分", "時間を分単位で").setRequired(true)
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val timerOption = event.getOption("タイマー")
            val minutesOption = event.getOption("分")

            //nullチェック
            if (timerOption == null || minutesOption == null) {
                replyCommandError(event)
                return
            }

            //時間を取得
            val minutes = minutesOption.asLong

            //タイマーの番号を取得
            val i = timerOption.asLong.toInt()

            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue()
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを延長
            timer.addTimer(minutes.toInt())

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").complete().delete().queue()
        }
    }

    /**
     * タイマーを一時停止させる
     */
    object Stop : SlashCommand("stop", "タイマーを一時停止する") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "一時停止するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("タイマー")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //タイマーの番号を取得
            val i = option.asLong.toInt()

            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue()
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを一時停止
            timer.stop()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").complete().delete().queue()
        }
    }

    /**
     * タイマーを再開する
     */
    object Restart : SlashCommand("restart", "タイマーの一時停止を再開する") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "再開するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("タイマー")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //タイマーの番号を取得
            val i = option.asLong.toInt()


            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue()
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを再開する
            timer.restart()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").complete().delete().queue()
        }
    }


    /**
     * タイマーを確認する
     */
    object Check : SlashCommand("check", "タイマーを延長する") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "確認するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val timerOption = event.getOption("タイマー")

            //nullチェック
            if (timerOption == null) {
                replyCommandError(event)
                return
            }

            //タイマーの番号を取得
            val i = timerOption.asLong.toInt()

            //チャンネルを取得
            val channel = event.textChannel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue()
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを確認
            timer.check()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").complete().delete().queue()
        }
    }

    /**
     * TTSの設定を行う
     */
    object TTS : SlashCommand("tts", "ttsによるメッセージの読み上げを設定する 初期状態ではLV0") {
        init {
            setDefaultEnabled(true)
            addSubcommands(
                SubcommandData("lv0", "TTSによる通知を行わない"),
                SubcommandData("lv1", "タイマー終了時のみ通知"),
                SubcommandData("lv2", "タイマー終了時と、定期的な時間通知の時に通知"),
                SubcommandData("lv3", "タイマーのすべての通知（LV2に加えて、延長など）で通知")
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //サブコマンドを取得
            val subCommand = event.subcommandName

            //nullチェック
            if (subCommand == null) {
                replyCommandError(event)
                return
            }

            //ttsのタイミングを取得
            val timing = when {
                subCommand.equalsIgnoreCase("lv1") -> ServerConfig.TTSTiming.LV1
                subCommand.equalsIgnoreCase("lv2") -> ServerConfig.TTSTiming.LV2
                subCommand.equalsIgnoreCase("lv3") -> ServerConfig.TTSTiming.LV3
                else -> ServerConfig.TTSTiming.LV0
            }

            //コンフィグへ保存
            val config = SimpleTimer.instance.config
            config.setTTS(event.guild!!, timing)
            config.save()

            //メッセージを出力
            event.hook.sendMessage("チャットの読み上げを${timing}にしました").queue()
        }
    }

    /**
     * 終了時のTTSのメッセージを変更
     */
    object FinishTTS : SlashCommand("finish_tts", "終了時のメッセージ読み上げの内容を変更する") {
        init {
            setDefaultEnabled(true)
            addOptions(OptionData(OptionType.STRING, "メッセージ", "メッセージの内容 タイマーの番号が'x'に代入されます").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("メッセージ")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //メッセージを取得
            val message = option.asString

            //メッセージの長さを確認
            if (message.length > 20) {
                event.hook.sendMessage("20文字以下にしてください").queue()
                return
            }

            //コンフィグへ保存
            val config = SimpleTimer.instance.config
            config.setFinishTTS(event.guild!!, message)
            config.save()

            //メッセージを出力
            event.hook.sendMessage("終了時のTTSメッセージを変更しました").queue()
        }
    }

    /**
     * メンション方式を変更する
     */
    object Mention : SlashCommand("mention", "メンションの方式を変更する") {
        init {
            setDefaultEnabled(true)
            addSubcommands(
                SubcommandData("here", "@hereを用いたメンション"),
                SubcommandData("vc", "ボイスチャットに接続されているメンバー")
                    .addOption(OptionType.CHANNEL, "channel", "メンションを行うボイスチャット（省略可）",false),
                SubcommandData("role", "ロールにメンションを行う")
                    .addOption(OptionType.ROLE, "role", "メンションを行うロール"),
                SubcommandData("off", "メンションを行わない")
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //サブコマンドを取得
            val subCommand = event.subcommandName

            //nullチェック
            if (subCommand == null) {
                replyCommandError(event)
                return
            }

            //メンションの方式を取得
            val mention = when (subCommand) {
                "off" -> ServerConfig.Mention.NONE
                "here" -> ServerConfig.Mention.HERE
                "role" -> ServerConfig.Mention.ROLE
                "vc" -> ServerConfig.Mention.VC
                else -> {
                    //エラーを出力
                    replyCommandError(event)
                    return
                }
            }

            //コンフィグへ設定
            val config = SimpleTimer.instance.config
            val guild = event.guild!!

            //VCの時に引数を確認
            if (mention == ServerConfig.Mention.VC){
                val option = event.getOption("channel")
                if (option != null){
                    val channel = option.asGuildChannel
                    if(channel is VoiceChannel){
                        config.setVCMentionTarget(guild, channel)
                    }else {
                        event.hook.sendMessage("ボイスチャットではないチャンネルです").queue()
                        return
                    }
                }else {
                    config.setVCMentionTarget(guild, null)
                }
            }

            //Roleの時に引数を確認
            if (mention == ServerConfig.Mention.ROLE){
                val option = event.getOption("role")
                if (option != null){
                    val role = option.asRole
                    config.setRoleMentionTarget(guild, role)
                }
            }

            //メンションの方式
            config.setMention(guild, mention)

            config.save()

            //メッセージを出力
            event.hook.sendMessage("メンションの設定を${mention}にしました").queue()
        }
    }
}