package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.modal.AddTimerModal
import dev.simpletimer.component.modal.StartTimerModal
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.*
import dev.simpletimer.timer.Timer
import dev.simpletimer.util.CommandUtil.createChoice
import dev.simpletimer.util.CommandUtil.createOptionData
import dev.simpletimer.util.CommandUtil.createSubCommandData
import dev.simpletimer.util.CommandUtil.replyCommandError
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class TimerCommands {

    /**
     * タイマーを開始する
     */
    object StartTimer : SlashCommandManager.SlashCommand(CommandInfoPath.TIMER, deferReply = false) {
        init {
            addOptions(createOptionData(OptionType.INTEGER, CommandInfoPath.MINUTES))
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.MINUTES)

            //nullチェック
            if (option == null) {
                //Timerを開始するModalを送信
                event.replyModal(StartTimerModal.createModal(0, event.guild!!.getLang())).queue()
                return
            }

            //長さを確認
            if (option.asLong * 60 > Int.MAX_VALUE) {
                replyCommandError(event)
                return
            }


            //とありあえず待たせる
            event.deferReply().queue()

            //チャンネルを取得
            val channel = event.guildChannel

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.entries) {
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
            event.hook.sendMessage(event.guild!!.getLang().timer.timerMaxWarning).queue()
        }
    }

    /**
     * タイマーを終了する
     */
    object Finish : SlashCommandManager.SlashCommand(CommandInfoPath.FINISH) {
        init {
            addOptions(
                createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_FINISH_TIMER).setRequired(true)
                    .addChoices(
                        createChoice(CommandInfoPath.ONE, 1),
                        createChoice(CommandInfoPath.TWO, 2),
                        createChoice(CommandInfoPath.THREE, 3),
                        createChoice(CommandInfoPath.FOUR, 4)
                    )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_FINISH_TIMER)

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
                val warning = event.guild!!.getLang().timer.timerNotMoveWarning

                //タイマーが稼働していないことを教えるメッセージを出力
                if (number != null)
                    event.hook.sendMessage(number.format(warning)).queue()
                else {
                    event.hook.sendMessage(warning).queue()
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
    object FinAll : SlashCommandManager.SlashCommand(CommandInfoPath.FINISH_ALL) {
        override fun run(event: SlashCommandInteractionEvent) {

            //チャンネルを取得
            val channel = event.channel

            //チャンネルのタイマーを取得
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //稼働しているタイマーの数を取得
            if (channelTimers.keys.size == 0) {
                event.hook.sendMessage(event.guild!!.getLang().timer.timerNotMoveWarning).queue()
                return
            }

            //タイマーをすべて終了させる
            for (number in Timer.Number.entries) {
                channelTimers[number]?.finish()
            }

            //空白を出力して消し飛ばす
            event.hook.sendEmpty()
        }
    }

    /**
     * タイマーを延長する
     */
    object Add : SlashCommandManager.SlashCommand(CommandInfoPath.ADD, false) {
        init {
            addOptions(
                createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_ADD_TIMER).setRequired(true).addChoices(
                    createChoice(CommandInfoPath.ONE, 1),
                    createChoice(CommandInfoPath.TWO, 2),
                    createChoice(CommandInfoPath.THREE, 3),
                    createChoice(CommandInfoPath.FOUR, 4)
                ),
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val timerOption = event.getOption(CommandInfoPath.TIMER_OPT_ADD_TIMER)
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
    object Stop : SlashCommandManager.SlashCommand(CommandInfoPath.STOP) {
        init {
            addOptions(
                createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_STOP_TIMER).setRequired(true).addChoices(
                    createChoice(CommandInfoPath.ONE, 1),
                    createChoice(CommandInfoPath.TWO, 2),
                    createChoice(CommandInfoPath.THREE, 3),
                    createChoice(CommandInfoPath.FOUR, 4)
                )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_STOP_TIMER)

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
                val warning = event.guild!!.getLang().timer.timerNotMoveWarning
                if (number != null)
                    event.hook.sendMessage(number.format(warning)).queue()
                else {
                    event.hook.sendMessage(warning).queue()
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
    object Restart : SlashCommandManager.SlashCommand(CommandInfoPath.RESTART) {
        init {
            addOptions(
                createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_RESTART_TIMER).setRequired(true)
                    .addChoices(
                        createChoice(CommandInfoPath.ONE, 1),
                        createChoice(CommandInfoPath.TWO, 2),
                        createChoice(CommandInfoPath.THREE, 3),
                        createChoice(CommandInfoPath.FOUR, 4)
                    )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_RESTART_TIMER)

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
                val warning = event.guild!!.getLang().timer.timerNotMoveWarning
                if (number != null)
                    event.hook.sendMessage(number.format(warning)).queue()
                else {
                    event.hook.sendMessage(warning).queue()
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
    object Check : SlashCommandManager.SlashCommand(CommandInfoPath.CHECK) {
        init {
            addOptions(
                createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_CHECK_TIMER).setRequired(true)
                    .addChoices(
                        createChoice(CommandInfoPath.ONE, 1),
                        createChoice(CommandInfoPath.TWO, 2),
                        createChoice(CommandInfoPath.THREE, 3),
                        createChoice(CommandInfoPath.FOUR, 4)
                    )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val timerOption = event.getOption(CommandInfoPath.TIMER_OPT_CHECK_TIMER)

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
                val warning = event.guild!!.getLang().timer.timerNotMoveWarning
                if (number != null)
                    event.hook.sendMessage(number.format(warning)).queue()
                else {
                    event.hook.sendMessage(warning).queue()
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
    object TTSTiming : SlashCommandManager.SlashCommand(CommandInfoPath.TTS_TIMING) {
        init {
            addSubcommands(
                createSubCommandData(CommandInfoPath.TIMER_SC_ZERO),
                createSubCommandData(CommandInfoPath.TIMER_SC_ONE),
                createSubCommandData(CommandInfoPath.TIMER_SC_TWO),
                createSubCommandData(CommandInfoPath.TIMER_SC_THREE)
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
            event.hook.sendMessage(guild.getLang().command.timer.ttsTiming.langFormat(timing)).queue()
        }
    }

    /**
     * 終了時のTTSのメッセージを変更
     */
    object FinishTTS : SlashCommandManager.SlashCommand(CommandInfoPath.TTS_FINISH_MESSAGE) {
        init {
            addOptions(
                createOptionData(OptionType.STRING, CommandInfoPath.TIMER_OPT_MESSAGE)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //メッセージを取得
            val message = event.getOption(CommandInfoPath.TIMER_OPT_MESSAGE)?.asString ?: ""

            val guild = event.guild!!

            //メッセージの長さを確認
            if (message.length > 20) {
                event.hook.sendMessage(guild.getLang().command.timer.maxMessageLengthWarning).queue()
                return
            }

            //ギルドのデータへ保存
            guild.getGuildData().finishTTS = message
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage(guild.getLang().command.timer.finishTTS).queue()
        }
    }

    /**
     * メンションのタイミングを設定する
     *
     */
    object MentionTiming : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_TIMING) {
        init {
            addSubcommands(
                createSubCommandData(CommandInfoPath.TIMER_SC_ZERO),
                createSubCommandData(CommandInfoPath.TIMER_SC_ONE),
                createSubCommandData(CommandInfoPath.TIMER_SC_TWO),
                createSubCommandData(CommandInfoPath.TIMER_SC_THREE)
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
            event.hook.sendMessage(guild.getLang().command.timer.mentionTiming.langFormat(timing)).queue()
        }
    }

    /**
     * メンション方式を変更する
     *
     */
    object Mention : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION) {
        init {
            addSubcommands(
                createSubCommandData(CommandInfoPath.TIMER_MENTION_HERE),
                createSubCommandData(CommandInfoPath.TIMER_MENTION_VC),
                createSubCommandData(CommandInfoPath.TIMER_MENTION_ROLE),
                createSubCommandData(CommandInfoPath.TIMER_MENTION_TARGET_VC),
                createSubCommandData(CommandInfoPath.TIMER_MENTION_OFF)
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

            //言語のデータ
            val langData = guild.getLang()

            //メッセージを出力
            event.hook.sendMessage(langData.command.timer.mentionSetting.langFormat(mention)).queue()

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
                        langData.command.timer.targetRole.langFormat(
                            list.filterNotNull().joinToString { "`${it.name}`" })
                    )
                } else {
                    //ロールがないことを結合
                    appendMessageBuffer.append(langData.command.timer.targetRoleEmpty)
                }
                //コマンドを結合
                appendMessageBuffer.append("\n${langData.command.timer.targetRolePrompt}")
            }
            if (mention == dev.simpletimer.data.enum.Mention.TARGET_VC) {
                //ターゲットのVCを確認する
                val list = guildData.vcMentionTargets
                //空かを確認
                if (list.isNotEmpty()) {
                    //ターゲットを結合
                    appendMessageBuffer.append(
                        langData.command.timer.targetVC.langFormat(list.filterNotNull().joinToString { "`${it.name}`" })
                    )
                } else {
                    //ターゲットがないことを結合
                    appendMessageBuffer.append(langData.command.timer.targetVCEmpty)
                }
                //コマンドを結合
                appendMessageBuffer.append("\n${langData.command.timer.targetVCPrompt}")
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
    object ShowRoleMentionTarget : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_ROLE) {
        override fun run(event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //言語のデータ
            val langData = guild.getLang()

            //ターゲットを取得
            val list = guild.getGuildData().roleMentionTargets

            //空かを確認し、メッセージを送信
            if (list.isEmpty()) {
                event.hook.sendMessage("${langData.command.timer.targetRoleEmpty}\n${langData.command.timer.targetRolePrompt}")
                    .queue()
            } else {
                event.hook.sendMessage(
                    "${
                        langData.command.timer.targetRole.langFormat(
                            list.filterNotNull().joinToString { "`${it.name}`" })
                    }\n${langData.command.timer.targetRolePrompt}"
                ).queue()
            }
        }
    }

    /**
     * メンションを行う対象のロールを追加する
     *
     */
    object AddRoleMentionTarget : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_ADD_ROLE) {
        init {
            addOptions(createOptionData(OptionType.ROLE, CommandInfoPath.TIMER_OPT_ADD_ROLE).setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_ADD_ROLE)

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
            event.hook.sendMessage(guild.getLang().command.timer.addRole.langFormat(role.name)).queue()
        }
    }

    /**
     * メンションを行う対象のロールを削除する
     *
     */
    object RemoveRoleMentionTarget : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_REMOVE_ROLE) {
        init {
            addOptions(createOptionData(OptionType.ROLE, CommandInfoPath.TIMER_OPT_REMOVE_ROLE).setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_REMOVE_ROLE)

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
            event.hook.sendMessage(guild.getLang().command.timer.removeRole.langFormat(role.name)).queue()
        }
    }

    /**
     * メンションを行う対象のボイスチャットを確認する
     *
     */
    object ShowVCMentionTarget : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_VC) {
        override fun run(event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //言語のデータ
            val langData = guild.getLang()

            //ギルドのデータを取得
            val list = guild.getGuildData().vcMentionTargets

            //空かを確認して、メッセージを送信
            if (list.isEmpty()) {
                event.hook.sendMessage("${langData.command.timer.targetVCEmpty}\n${langData.command.timer.targetVCPrompt}")
                    .queue()
            } else {
                event.hook.sendMessage(
                    "${
                        langData.command.timer.targetVC.langFormat(
                            list.filterNotNull().joinToString { "`${it.name}`" })
                    }\n${langData.command.timer.targetVCPrompt}"
                ).queue()
            }
        }
    }

    /**
     * メンションを行う対象のボイスチャットを追加する
     *
     */
    object AddVCMentionTarget : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_ADD_VC) {
        init {
            addOptions(
                createOptionData(OptionType.CHANNEL, CommandInfoPath.TIMER_OPT_ADD_VC).setChannelTypes(
                    ChannelType.VOICE,
                    ChannelType.STAGE
                ).setRequired(true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_ADD_VC)

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            val langData = event.guild?.getLang() ?: return

            //ロール名を取得
            val channel = option.asChannel
            if (channel !is AudioChannel) {
                event.hook.sendMessage(langData.command.timer.notVCWaring).queue()
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
            event.hook.sendMessage(langData.command.timer.addVC.langFormat(channel.asGuildMessageChannel().name))
                .queue()
        }
    }

    /**
     * メンションを行う対象のボイスチャットを削除する
     *
     */
    object RemoveVCMentionTarget : SlashCommandManager.SlashCommand(CommandInfoPath.MENTION_REMOVE_VC) {
        init {
            addOptions(
                createOptionData(OptionType.CHANNEL, CommandInfoPath.TIMER_OPT_REMOVE_VC).setChannelTypes(
                    ChannelType.VOICE,
                    ChannelType.STAGE
                ).setRequired(true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption(CommandInfoPath.TIMER_OPT_REMOVE_VC)

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //言語のデータ
            val langData = event.guild?.getLang() ?: return

            //ロール名を取得
            val channel = option.asChannel
            if (channel !is AudioChannel) {
                event.hook.sendMessage(langData.command.timer.notVCWaring).queue()
                return
            }

            //ギルドのデータから削除
            val guild = event.guild!!
            guild.getGuildData().vcMentionTargets.removeIf { it?.id == channel.asGuildMessageChannel().id }
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage(langData.command.timer.removeVC.langFormat(channel.asGuildMessageChannel().name))
                .queue()
        }
    }
}