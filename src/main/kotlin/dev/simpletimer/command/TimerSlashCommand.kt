package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.getGuildData
import dev.simpletimer.timer.Timer
import dev.simpletimer.util.equalsIgnoreCase
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*

class TimerSlashCommand {

    /**
     * タイマーを開始する
     */
    object StartTimer : SlashCommand("timer", "タイマーを開始する") {
        init {
            isDefaultEnabled = true

            addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
            val channel = event.channel

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始
                    val timer = Timer(channel, number, minutes.toInt(), event.guild!!)

                    //タイマーのインスタンスを代入する
                    channelTimers[number] = timer
                    Timer.channelsTimersMap[channel] = channelTimers

                    //空白を出力して消し飛ばす
                    event.hook.sendMessage("|| ||").queue {
                        it.delete().queue({}, {})
                    }
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）").queue({}, {})
        }
    }

    /**
     * タイマーを終了する
     */
    object Finish : SlashCommand("finish", "タイマーを終了する") {
        init {
            isDefaultEnabled = true

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "終了するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                //タイマーが稼働していないことを教えるメッセージを出力
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue({}, {})
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを終わらせる
            timer.finish()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").queue {
                it.delete().queue({}, {})
            }
        }
    }

    /**
     * すべてのタイマーを終了
     */
    object FinAll : SlashCommand("finish_all", "すべてのタイマーを終了する") {
        init {
            isDefaultEnabled = true
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {

            //チャンネルを取得
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //稼働しているタイマーの数を取得
            if (channelTimers.keys.size == 0) {
                event.hook.sendMessage("タイマーは動いていません").queue({}, {})
                return
            }

            //タイマーをすべて終了させる
            for (number in Timer.Number.values()) {
                channelTimers[number]?.finish()
            }

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").queue {
                it.delete().queue({}, {})
            }
        }
    }

    /**
     * タイマーを延長する
     */
    object Add : SlashCommand("add", "タイマーを延長する") {
        init {
            isDefaultEnabled = true

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

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue({}, {})
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを延長
            timer.add(minutes.toInt() * 60)

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").queue {
                it.delete().queue({}, {})
            }
        }
    }

    /**
     * タイマーを一時停止させる
     */
    object Stop : SlashCommand("stop", "タイマーを一時停止する") {
        init {
            isDefaultEnabled = true

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "一時停止するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue({}, {})
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを一時停止
            timer.stop()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").queue {
                it.delete().queue({}, {})
            }
        }
    }

    /**
     * タイマーを再開する
     */
    object Restart : SlashCommand("restart", "タイマーの一時停止を再開する") {
        init {
            isDefaultEnabled = true

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "再開するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue({}, {})
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを再開する
            timer.restart()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").queue {
                it.delete().queue({}, {})
            }
        }
    }


    /**
     * タイマーを確認する
     */
    object Check : SlashCommand("check", "タイマーを延長する") {
        init {
            isDefaultEnabled = true

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "確認するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                )
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号をNumberに
            val number = Timer.Number.getNumber(i)

            //タイマーの稼働を確認
            if (!channelTimers.containsKey(number)) {
                if (number != null)
                    event.hook.sendMessage(number.format("タイマーは動いていません")).queue({}, {})
                else {
                    event.hook.sendMessage("タイマーは動いていません").queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを確認
            timer.check()

            //空白を出力して消し飛ばす
            event.hook.sendMessage("|| ||").queue {
                it.delete().queue({}, {})
            }
        }
    }

    /**
     * TTSの設定を行う
     */
    object TTSTiming : SlashCommand("tts_timing", "ttsによるメッセージの読み上げを設定する 初期状態ではLV0") {
        init {
            isDefaultEnabled = true
            addSubcommands(
                SubcommandData("lv0", "通知を行わない"),
                SubcommandData("lv1", "タイマー終了時のみ通知"),
                SubcommandData("lv2", "タイマー終了時と、定期的な時間通知の時に通知"),
                SubcommandData("lv3", "タイマーのすべての通知（LV2に加えて、延長など）で通知")
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //サブコマンドを取得
            val subCommand = event.subcommandName

            //nullチェック
            if (subCommand == null) {
                replyCommandError(event)
                return
            }

            //ttsのタイミングを取得
            val timing = when {
                subCommand.equalsIgnoreCase("lv1") -> NoticeTiming.LV1
                subCommand.equalsIgnoreCase("lv2") -> NoticeTiming.LV2
                subCommand.equalsIgnoreCase("lv3") -> NoticeTiming.LV3
                else -> NoticeTiming.LV0
            }

            //ギルドのデータへ保存
            event.guild!!.getGuildData().ttsTiming = timing
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("チャットの読み上げを${timing}にしました").queue({}, {})
        }
    }

    /**
     * 終了時のTTSのメッセージを変更
     */
    object FinishTTS : SlashCommand("tts_finishmessage", "終了時のメッセージ読み上げの内容を変更する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.STRING, "メッセージ", "メッセージの内容 タイマーの番号が'x'に代入されます").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
                event.hook.sendMessage("20文字以下にしてください").queue({}, {})
                return
            }

            //ギルドのデータへ保存
            event.guild!!.getGuildData().finishTTS = message
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("終了時のTTSメッセージを変更しました").queue({}, {})
        }
    }

    object MentionTiming : SlashCommand("mention_timing", "メンションを行うタイミングの設定をする 初期状態ではLV2") {
        init {
            isDefaultEnabled = true
            addSubcommands(
                SubcommandData("lv0", "通知を行わない"),
                SubcommandData("lv1", "タイマー終了時のみ通知"),
                SubcommandData("lv2", "タイマー終了時と、定期的な時間通知の時に通知"),
                SubcommandData("lv3", "タイマーのすべての通知（LV2に加えて、延長など）で通知")
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //サブコマンドを取得
            val subCommand = event.subcommandName

            //nullチェック
            if (subCommand == null) {
                replyCommandError(event)
                return
            }

            //ttsのタイミングを取得
            val timing = when {
                subCommand.equalsIgnoreCase("lv1") -> NoticeTiming.LV1
                subCommand.equalsIgnoreCase("lv2") -> NoticeTiming.LV2
                subCommand.equalsIgnoreCase("lv3") -> NoticeTiming.LV3
                else -> NoticeTiming.LV0
            }

            //ギルドのデータへ保存
            event.guild!!.getGuildData().mentionTiming = timing
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("メンションを行うタイミングを${timing}にしました").queue({}, {})
        }
    }

    /**
     * メンション方式を変更する
     */
    object Mention : SlashCommand("mention", "メンションの方式を変更する") {
        init {
            isDefaultEnabled = true
            addSubcommands(
                SubcommandData("here", "@hereを用いたメンション"),
                SubcommandData("vc", "ボイスチャットに接続されているメンバー"),
                SubcommandData("role", "特定のロールにメンション"),
                SubcommandData("target_vc", "特定のボイスチャットに接続されているメンバー"),
                SubcommandData("off", "メンションを行わない")
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //サブコマンドを取得
            val subCommand = event.subcommandName

            //nullチェック
            if (subCommand == null) {
                replyCommandError(event)
                return
            }

            //メンションの方式を取得
            val mention = when (subCommand) {
                "off" -> dev.simpletimer.data.enum.Mention.NONE
                "here" -> dev.simpletimer.data.enum.Mention.HERE
                "role" -> dev.simpletimer.data.enum.Mention.ROLE
                "vc" -> dev.simpletimer.data.enum.Mention.VC
                "target_vc" -> dev.simpletimer.data.enum.Mention.TARGET_VC
                else -> {
                    //エラーを出力
                    replyCommandError(event)
                    return
                }
            }

            val guild = event.guild!!

            //ギルドのデータ取得
            val guildData = guild.getGuildData()

            //ギルドのデータへ保存
            guildData.mention = mention
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("メンションの設定を${mention}にしました").queue({}, {})

            //追加を促す
            val appendMessageBuffer = StringBuffer()
            if (mention == dev.simpletimer.data.enum.Mention.ROLE) {
                val list = guildData.roleMentionTargets
                if (list.isNotEmpty()) {
                    appendMessageBuffer.append(
                        "メンションを行う対象のロールは${
                            list.filterNotNull().joinToString { "`${it.name}`" }
                        }です。"
                    )
                } else {
                    appendMessageBuffer.append("メンションを行う対象のロールが設定されていません。")
                }
                appendMessageBuffer.append("\n対象のロールは、`/mention_addrole`で追加できます。")
            }
            if (mention == dev.simpletimer.data.enum.Mention.TARGET_VC) {
                val list = guildData.vcMentionTargets
                if (list.isNotEmpty()) {
                    appendMessageBuffer.append(
                        "メンションを行う対象のボイスチャンネルは${
                            list.filterNotNull().joinToString { "`${it.name}`" }
                        }です。"
                    )
                } else {
                    appendMessageBuffer.append("メンションを行う対象のボイスチャンネルが設定されていません。")
                }
                appendMessageBuffer.append("\n対象のボイスチャンネルは、`/mention_addvc`で追加できます。")
            }
            val appendMessage = appendMessageBuffer.toString()
            if (appendMessage != "") {
                event.hook.sendMessage(appendMessage).queue({}, {})
            }
        }
    }

    object ShowRoleMentionTarget : SlashCommand("mention_role", "メンションを行う対象のロールを確認する") {
        init {
            isDefaultEnabled = true
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            val list = guildData.roleMentionTargets

            if (list.isEmpty()) {
                event.hook.sendMessage(
                    """
                メンションを行う対象のロールがありません。
                対象のロールは、`/mention_addrole`で追加できます。
                """.trimIndent()
                ).queue({}, {})
            } else {
                event.hook.sendMessage(
                    """
                メンションを行う対象のロールは${list.filterNotNull().joinToString { "`${it.name}`" }}です。
                対象のロールは、`/mention_addrole`で追加できます。
                """.trimIndent()
                ).queue({}, {})
            }
        }
    }

    object AddRoleMentionTarget : SlashCommand("mention_addrole", "メンションを行う対象のロールを追加する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.ROLE, "role", "追加するロール").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("role")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //ロール名を取得
            val role = option.asRole

            //ギルドのデータに保管
            val guildData = event.guild?.getGuildData() ?: return
            guildData.roleMentionTargets.add(role)
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("`${role.name}`をメンション対象に追加しました").queue({}, {})
        }
    }

    object RemoveRoleMentionTarget : SlashCommand("mention_removerole", "メンションを行う対象のロールを追加する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.ROLE, "role", "追加するロール").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("role")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //ロール名を取得
            val role = option.asRole

            //ギルドのデータへ追加
            val guildData = event.guild?.getGuildData() ?: return
            guildData.roleMentionTargets.remove(role)
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("`${role.name}`をメンション対象から削除しました").queue({}, {})
        }
    }

    object ShowVCMentionTarget : SlashCommand("mention_vc", "メンションを行う対象のボイスチャットを確認する") {
        init {
            isDefaultEnabled = true
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            val list = guildData.vcMentionTargets
            if (list.isEmpty()) {
                event.hook.sendMessage(
                    """
                メンションを行う対象のボイスチャンネルがありません。
                対象のチャンネルは、`/mention_addvc`で追加できます。
                """.trimIndent()
                ).queue({}, {})
            } else {
                event.hook.sendMessage(
                    """
                メンションを行う対象のボイスチャンネルは${list.filterNotNull().joinToString { "`${it.name}`" }}です。
                対象のチャンネルは、`/mention_addvc`で追加できます。
                """.trimIndent()
                ).queue({}, {})
            }
        }
    }

    object AddVCMentionTarget : SlashCommand("mention_addvc", "メンションを行う対象のボイスチャットを追加する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.CHANNEL, "channel", "追加するボイスチャット").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("channel")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //ロール名を取得
            val channel = option.asGuildChannel
            if (channel !is VoiceChannel) {
                event.hook.sendMessage("ボイスチャットではないチャンネルです").queue({}, {})
                return
            }

            //ギルドのデータへ追加
            event.guild!!.getGuildData().vcMentionTargets.add(channel)
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("`${channel.name}`をメンション対象に追加しました").queue({}, {})
        }
    }

    object RemoveVCMentionTarget : SlashCommand("mention_removevc", "メンションを行う対象のボイスチャットを削除する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.CHANNEL, "channel", "削除するボイスチャット").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("channel")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //ロール名を取得
            val channel = option.asGuildChannel
            if (channel !is VoiceChannel) {
                event.hook.sendMessage("ボイスチャットではないチャンネルです").queue({}, {})
                return
            }

            //ギルドのデータから削除
            event.guild!!.getGuildData().vcMentionTargets.remove(channel)
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを出力
            event.hook.sendMessage("`${channel.name}`をメンション対象から削除しました").queue({}, {})
        }
    }
}