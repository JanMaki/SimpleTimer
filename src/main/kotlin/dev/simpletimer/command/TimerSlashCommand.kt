package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.modal.AddTimerModal
import dev.simpletimer.component.modal.StartTimerModal
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.timer.Timer
import dev.simpletimer.util.equalsIgnoreCase
import dev.simpletimer.util.getGuildData
import dev.simpletimer.util.sendEmpty
import dev.simpletimer.util.sendMessage
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
    object StartTimer : SlashCommand("timer", "タイマーを開始する", beforeReply = false) {
        init {
            isDefaultEnabled = true

            addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で"))
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("分")

            //nullチェック
            if (option == null) {
                //Timerを開始するModalを送信
                event.replyModal(StartTimerModal.createModal(0)).queue({}, {})
                return
            }

            //とありあえず待たせる
            event.deferReply().queue({}, {})

            //チャンネルを取得
            val channel = event.channel

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始し、インスタンスを代入する
                    channelTimers[number] = Timer(channel, number,  option.asInt * 60, event.guild!!)
                    Timer.channelsTimersMap[channel] = channelTimers

                    //空白を出力して消し飛ばす
                    event.hook.sendEmpty()
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）", true).queue({}, {})
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

        override fun run(event: SlashCommandInteractionEvent) {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません"), true).queue({}, {})
                else {
                    event.hook.sendMessage("*タイマーは動いていません", true).queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを終わらせる
            timer.finish()

            //空白を出力して消し飛ばす
            event.hook.sendEmpty()
        }
    }

    /**
     * すべてのタイマーを終了
     */
    object FinAll : SlashCommand("finish_all", "すべてのタイマーを終了する") {
        init {
            isDefaultEnabled = true
        }

        override fun run(event: SlashCommandInteractionEvent) {

            //チャンネルを取得
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //稼働しているタイマーの数を取得
            if (channelTimers.keys.size == 0) {
                event.hook.sendMessage("*タイマーは動いていません", true).queue({}, {})
                return
            }

            //タイマーをすべて終了させる
            for (number in Timer.Number.values()) {
                channelTimers[number]?.finish()
            }

            //空白を出力して消し飛ばす
            event.hook.sendEmpty()
        }
    }

    /**
     * タイマーを延長する
     */
    object Add : SlashCommand("add", "タイマーを延長する", false) {
        init {
            isDefaultEnabled = true

            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "延長するタイマー").setRequired(true).addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                ),
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val timerOption = event.getOption("タイマー")
            //nullチェック
            if (timerOption == null){
                replyCommandError(event)
                return
            }

            //タイマーの番号を取得
            val number = Timer.Number.getNumber(timerOption.asLong.toInt())
            //nullチェック
            if (number == null){
                replyCommandError(event)
                return
            }

            //Modalを送信
            event.replyModal(AddTimerModal.createModal(number)).queue({}, {})
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

        override fun run(event: SlashCommandInteractionEvent) {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません"), true).queue({}, {})
                else {
                    event.hook.sendMessage("*タイマーは動いていません", true).queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを一時停止
            timer.stop()

            //空白を出力して消し飛ばす
            event.hook.sendEmpty()
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

        override fun run(event: SlashCommandInteractionEvent) {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません"), true).queue({}, {})
                else {
                    event.hook.sendMessage("*タイマーは動いていません", true).queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを再開する
            timer.restart()

            //空白を出力して消し飛ばす
            event.hook.sendEmpty()
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

        override fun run(event: SlashCommandInteractionEvent) {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません"), true).queue({}, {})
                else {
                    event.hook.sendMessage("*タイマーは動いていません", true).queue({}, {})
                }
                return
            }

            //タイマーを取得
            val timer = channelTimers[number]!!

            //タイマーを確認
            timer.check()

            //空白を出力して消し飛ばす
            event.hook.sendEmpty()
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

        override fun run(event: SlashCommandInteractionEvent) {
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
            val guild = event.guild!!
            guild.getGuildData().ttsTiming = timing
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

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
            addOptions(
                OptionData(
                    OptionType.STRING,
                    "メッセージ",
                    "メッセージの内容 タイマーの番号が'x'に代入されます 何も入力しないと、終了時にTTSの読み上げを行いません"
                )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //メッセージを取得
            val message = event.getOption("メッセージ")?.asString ?: ""

            //メッセージの長さを確認
            if (message.length > 20) {
                event.hook.sendMessage("*20文字以下にしてください", true).queue({}, {})
                return
            }

            //ギルドのデータへ保存
            val guild = event.guild!!
            guild.getGuildData().finishTTS = message
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("終了時のTTSメッセージを変更しました").queue({}, {})
        }
    }

    /**
     * メンションのタイミングを設定する
     *
     */
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

        override fun run(event: SlashCommandInteractionEvent) {
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
            val guild = event.guild!!
            guild.getGuildData().mentionTiming = timing
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("メンションを行うタイミングを${timing}にしました").queue({}, {})
        }
    }

    /**
     * メンション方式を変更する
     *
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

        override fun run(event: SlashCommandInteractionEvent) {
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
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("メンションの設定を${mention}にしました").queue({}, {})

            //追加を促すメッセージ
            val appendMessageBuffer = StringBuffer()
            //設定を確認
            if (mention == dev.simpletimer.data.enum.Mention.ROLE) {
                //ターゲットのロールを確認する
                val list = guildData.roleMentionTargets
                //空かを確認
                if (list.isNotEmpty()) {
                    //ターゲットを結合
                    appendMessageBuffer.append(
                        "メンションを行う対象のロールは${
                            list.filterNotNull().joinToString { "`${it.name}`" }
                        }です。"
                    )
                } else {
                    //ロールがないことを結合
                    appendMessageBuffer.append("メンションを行う対象のロールが設定されていません。")
                }
                //コマンドを結合
                appendMessageBuffer.append("\n対象のロールは、`/mention_addrole`で追加できます。")
            }
            if (mention == dev.simpletimer.data.enum.Mention.TARGET_VC) {
                //ターゲットのVCを確認する
                val list = guildData.vcMentionTargets
                //空かを確認
                if (list.isNotEmpty()) {
                    //ターゲットを結合
                    appendMessageBuffer.append(
                        "メンションを行う対象のボイスチャンネルは${
                            list.filterNotNull().joinToString { "`${it.name}`" }
                        }です。"
                    )
                } else {
                    //ターゲットがないことを結合
                    appendMessageBuffer.append("メンションを行う対象のボイスチャンネルが設定されていません。")
                }
                //コマンドを結合
                appendMessageBuffer.append("\n対象のボイスチャンネルは、`/mention_addvc`で追加できます。")
            }
            //StringBufferを文字列に
            val appendMessage = appendMessageBuffer.toString()
            //空だと何もしない
            if (appendMessage != "") {
                //メッセージを送信
                event.hook.sendMessage(appendMessage, true).queue({}, {})
            }
        }
    }

    /**
     * メンションを行う対象のロールを確認する
     *
     */
    object ShowRoleMentionTarget : SlashCommand("mention_role", "メンションを行う対象のロールを確認する") {
        init {
            isDefaultEnabled = true
        }

        override fun run(event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //ターゲットを取得
            val list = guild.getGuildData().roleMentionTargets

            //空かを確認し、メッセージを送信
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

    /**
     * メンションを行う対象のロールを追加する
     *
     */
    object AddRoleMentionTarget : SlashCommand("mention_addrole", "メンションを行う対象のロールを追加する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.ROLE, "role", "追加するロール").setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
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
            val guild = event.guild!!
            val guildData = guild.getGuildData()
            guildData.roleMentionTargets.add(role)
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${role.name}`をメンション対象に追加しました").queue({}, {})
        }
    }

    /**
     * メンションを行う対象のロールを追加する
     *
     */
    object RemoveRoleMentionTarget : SlashCommand("mention_removerole", "メンションを行う対象のロールを追加する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.ROLE, "role", "追加するロール").setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
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
            val guild = event.guild!!
            val guildData = guild.getGuildData()
            guildData.roleMentionTargets.remove(role)
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${role.name}`をメンション対象から削除しました").queue({}, {})
        }
    }

    /**
     * メンションを行う対象のボイスチャットを確認する
     *
     */
    object ShowVCMentionTarget : SlashCommand("mention_vc", "メンションを行う対象のボイスチャットを確認する") {
        init {
            isDefaultEnabled = true
        }

        override fun run(event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //ギルドのデータを取得
            val list = guild.getGuildData().vcMentionTargets

            //空かを確認して、メッセージを送信
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

    /**
     * メンションを行う対象のボイスチャットを追加する
     *
     */
    object AddVCMentionTarget : SlashCommand("mention_addvc", "メンションを行う対象のボイスチャットを追加する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.CHANNEL, "channel", "追加するボイスチャット").setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
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
                event.hook.sendMessage("ボイスチャットではないチャンネルです", true).queue({}, {})
                return
            }

            //ギルドのデータへ追加
            val guild = event.guild!!
            guild.getGuildData().vcMentionTargets.add(channel)
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${channel.name}`をメンション対象に追加しました").queue({}, {})
        }
    }

    /**
     * メンションを行う対象のボイスチャットを削除する
     *
     */
    object RemoveVCMentionTarget : SlashCommand("mention_removevc", "メンションを行う対象のボイスチャットを削除する") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.CHANNEL, "channel", "削除するボイスチャット").setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
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
                event.hook.sendMessage("*ボイスチャットではないチャンネルです", true).queue({}, {})
                return
            }

            //ギルドのデータから削除
            val guild = event.guild!!
            guild.getGuildData().vcMentionTargets.remove(channel)
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${channel.name}`をメンション対象から削除しました").queue({}, {})
        }
    }
}