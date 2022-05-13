package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.modal.AddTimerModal
import dev.simpletimer.component.modal.StartTimerModal
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.extension.*
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.AudioChannel
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*

class TimerCommands {

    /**
     * タイマーを開始する
     */
    object StartTimer : SlashCommandManager.SlashCommand("timer", "タイマーを開始する", deferReply = false) {
        init {
            addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で"))
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("分")

            //nullチェック
            if (option == null) {
                //Timerを開始するModalを送信
                event.replyModal(StartTimerModal.createModal(0, event.guild!!.getLang())).queue()
                return
            }

            //とありあえず待たせる
            event.deferReply().queue()

            //チャンネルを取得
            val channel = event.guildChannel

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始し、インスタンスを代入する
                    channelTimers[number] = Timer(channel, number, option.asInt * 60, event.guild!!)
                    Timer.channelsTimersMap[channel] = channelTimers

                    //空白を出力して消し飛ばす
                    event.hook.sendEmpty()
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
    object Finish : SlashCommandManager.SlashCommand("finish", "タイマーを終了する") {
        init {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("*タイマーは動いていません").queue()
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
    object FinAll : SlashCommandManager.SlashCommand("finish_all", "すべてのタイマーを終了する") {
        override fun run(event: SlashCommandInteractionEvent) {

            //チャンネルを取得
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //稼働しているタイマーの数を取得
            if (channelTimers.keys.size == 0) {
                event.hook.sendMessage("*タイマーは動いていません").queue()
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
    object Add : SlashCommandManager.SlashCommand("add", "タイマーを延長する", false) {
        init {
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
            if (timerOption == null) {
                replyCommandError(event)
                return
            }

            //タイマーの番号を取得
            val number = Timer.Number.getNumber(timerOption.asLong.toInt())
            //nullチェック
            if (number == null) {
                replyCommandError(event)
                return
            }

            //Modalを送信
            event.replyModal(AddTimerModal.createModal(number, event.guild!!.getLang())).queue()
        }
    }

    /**
     * タイマーを一時停止させる
     */
    object Stop : SlashCommandManager.SlashCommand("stop", "タイマーを一時停止する") {
        init {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("*タイマーは動いていません").queue()
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
    object Restart : SlashCommandManager.SlashCommand("restart", "タイマーの一時停止を再開する") {
        init {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("*タイマーは動いていません").queue()
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
    object Check : SlashCommandManager.SlashCommand("check", "タイマーを確認する") {
        init {
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
                    event.hook.sendMessage(number.format("*タイマーは動いていません")).queue()
                else {
                    event.hook.sendMessage("*タイマーは動いていません").queue()
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
    object TTSTiming : SlashCommandManager.SlashCommand("tts_timing", "ttsによるメッセージの読み上げを設定する 初期状態ではLV0") {
        init {
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
            event.hook.sendMessage("チャットの読み上げを${timing}にしました").queue()
        }
    }

    /**
     * 終了時のTTSのメッセージを変更
     */
    object FinishTTS : SlashCommandManager.SlashCommand("tts_finishmessage", "終了時のメッセージ読み上げの内容を変更する") {
        init {
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
                event.hook.sendMessage("*20文字以下にしてください").queue()
                return
            }

            //ギルドのデータへ保存
            val guild = event.guild!!
            guild.getGuildData().finishTTS = message
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("終了時のTTSメッセージを変更しました").queue()
        }
    }

    /**
     * メンションのタイミングを設定する
     *
     */
    object MentionTiming : SlashCommandManager.SlashCommand("mention_timing", "メンションを行うタイミングの設定をする 初期状態ではLV2") {
        init {
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
            event.hook.sendMessage("メンションを行うタイミングを${timing}にしました").queue()
        }
    }

    /**
     * メンション方式を変更する
     *
     */
    object Mention : SlashCommandManager.SlashCommand("mention", "メンションの方式を変更する") {
        init {
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
            event.hook.sendMessage("メンションの設定を${mention}にしました").queue()

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
                event.hook.sendMessage(appendMessage).queue()
            }
        }
    }

    /**
     * メンションを行う対象のロールを確認する
     *
     */
    object ShowRoleMentionTarget : SlashCommandManager.SlashCommand("mention_role", "メンションを行う対象のロールを確認する") {
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
                ).queue()
            } else {
                event.hook.sendMessage(
                    """
                メンションを行う対象のロールは${list.filterNotNull().joinToString { "`${it.name}`" }}です。
                対象のロールは、`/mention_addrole`で追加できます。
                """.trimIndent()
                ).queue()
            }
        }
    }

    /**
     * メンションを行う対象のロールを追加する
     *
     */
    object AddRoleMentionTarget : SlashCommandManager.SlashCommand("mention_addrole", "メンションを行う対象のロールを追加する") {
        init {
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
            event.hook.sendMessage("`${role.name}`をメンション対象に追加しました").queue()
        }
    }

    /**
     * メンションを行う対象のロールを削除する
     *
     */
    object RemoveRoleMentionTarget : SlashCommandManager.SlashCommand("mention_removerole", "メンションを行う対象のロールを削除する") {
        init {
            addOptions(OptionData(OptionType.ROLE, "role", "削除するロール").setRequired(true))
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

            //ギルドのデータから削除
            val guild = event.guild!!
            guild.getGuildData().roleMentionTargets.removeIf { it?.id == role.id }
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${role.name}`をメンション対象から削除しました").queue()
        }
    }

    /**
     * メンションを行う対象のボイスチャットを確認する
     *
     */
    object ShowVCMentionTarget : SlashCommandManager.SlashCommand("mention_vc", "メンションを行う対象のボイスチャットを確認する") {
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
                ).queue()
            } else {
                event.hook.sendMessage(
                    """
                メンションを行う対象のボイスチャンネルは${list.filterNotNull().joinToString { "`${it.name}`" }}です。
                対象のチャンネルは、`/mention_addvc`で追加できます。
                """.trimIndent()
                ).queue()
            }
        }
    }

    /**
     * メンションを行う対象のボイスチャットを追加する
     *
     */
    object AddVCMentionTarget : SlashCommandManager.SlashCommand("mention_addvc", "メンションを行う対象のボイスチャットを追加する") {
        init {
            addOptions(
                OptionData(OptionType.CHANNEL, "channel", "追加するボイスチャット").setChannelTypes(
                    ChannelType.VOICE,
                    ChannelType.STAGE
                ).setRequired(true)
            )
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
            if (channel !is AudioChannel) {
                event.hook.sendMessage("ボイスチャットではないチャンネルです").queue()
                return
            }

            //権限を確認
            if (!channel.checkSimpleTimerPermission()) {
                event.hook.sendMessageEmbeds(SimpleTimer.instance.getErrorEmbed(channel)).queue()
                return
            }

            //ギルドのデータへ追加
            val guild = event.guild!!
            guild.getGuildData().vcMentionTargets.add(channel)
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${channel.name}`をメンション対象に追加しました").queue()
        }
    }

    /**
     * メンションを行う対象のボイスチャットを削除する
     *
     */
    object RemoveVCMentionTarget : SlashCommandManager.SlashCommand("mention_removevc", "メンションを行う対象のボイスチャットを削除する") {
        init {
            addOptions(
                OptionData(OptionType.CHANNEL, "channel", "削除するボイスチャット").setChannelTypes(
                    ChannelType.VOICE,
                    ChannelType.STAGE
                ).setRequired(true)
            )
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
            if (channel !is AudioChannel) {
                event.hook.sendMessage("*ボイスチャットではないチャンネルです").queue()
                return
            }

            //ギルドのデータから削除
            val guild = event.guild!!
            guild.getGuildData().vcMentionTargets.removeIf { it?.id == channel.id }
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("`${channel.name}`をメンション対象から削除しました").queue()
        }
    }
}