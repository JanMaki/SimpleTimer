package dev.simpletimer.command.timer

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.*
import dev.simpletimer.util.CommandUtil
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType


/**
 * メンションのタイミングを設定する
 *
 */
object MentionTiming : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_TIMING) {
    init {
        addOptions(
            CommandUtil.createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_TIMING, true)
                .addChoices(
                    CommandUtil.createChoice(CommandInfoPath.TIMER_SC_ZERO, 0),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_SC_ONE, 1),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_SC_TWO, 2),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_SC_THREE, 3),
                )
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val timingOption = event.getOption(CommandInfoPath.TIMER_OPT_TIMING)

        //nullチェック
        if (timingOption == null) {
            CommandUtil.replyCommandError(event)
            return
        }


        //ttsのタイミングを取得
        val timing = when (timingOption.asLong.toInt()) {
            1 -> NoticeTiming.LV1
            2 -> NoticeTiming.LV2
            3 -> NoticeTiming.LV3
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
object MentionType : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_TYPE) {
    init {
        MentionTiming.addOptions(
            CommandUtil.createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_TYPE, true)
                .addChoices(
                    CommandUtil.createChoice(CommandInfoPath.TIMER_MENTION_HERE, 0),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_MENTION_VC, 1),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_MENTION_ROLE, 2),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_MENTION_TARGET_VC, 3),
                    CommandUtil.createChoice(CommandInfoPath.TIMER_MENTION_OFF, 4)
                )
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val timingOption = event.getOption(CommandInfoPath.TIMER_OPT_TYPE)

        //nullチェック
        if (timingOption == null) {
            CommandUtil.replyCommandError(event)
            return
        }

        //メンションの方式を取得
        val mention = when (timingOption.asLong.toInt()) {
            4 -> dev.simpletimer.data.enum.Mention.NONE
            0 -> dev.simpletimer.data.enum.Mention.HERE
            2 -> dev.simpletimer.data.enum.Mention.ROLE
            1 -> dev.simpletimer.data.enum.Mention.VC
            3 -> dev.simpletimer.data.enum.Mention.TARGET_VC
            else -> {
                //エラーを出力
                CommandUtil.replyCommandError(event)
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
object ShowRoleMentionTarget : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_ROLE) {
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
object AddRoleMentionTarget : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_ADD_ROLE) {
    init {
        addOptions(CommandUtil.createOptionData(OptionType.ROLE, CommandInfoPath.TIMER_OPT_ADD_ROLE).setRequired(true))
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val option = event.getOption(CommandInfoPath.TIMER_OPT_ADD_ROLE)

        //nullチェック
        if (option == null) {
            CommandUtil.replyCommandError(event)
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
object RemoveRoleMentionTarget : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_REMOVE_ROLE) {
    init {
        addOptions(
            CommandUtil.createOptionData(OptionType.ROLE, CommandInfoPath.TIMER_OPT_REMOVE_ROLE).setRequired(true)
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val option = event.getOption(CommandInfoPath.TIMER_OPT_REMOVE_ROLE)

        //nullチェック
        if (option == null) {
            CommandUtil.replyCommandError(event)
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
object ShowVCMentionTarget : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_VC) {
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
object AddVCMentionTarget : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_ADD_VC) {
    init {
        addOptions(
            CommandUtil.createOptionData(OptionType.CHANNEL, CommandInfoPath.TIMER_OPT_ADD_VC).setChannelTypes(
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
            CommandUtil.replyCommandError(event)
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
object RemoveVCMentionTarget : SlashCommandManager.SubCommand(CommandInfoPath.MENTION_REMOVE_VC) {
    init {
        addOptions(
            CommandUtil.createOptionData(OptionType.CHANNEL, CommandInfoPath.TIMER_OPT_REMOVE_VC).setChannelTypes(
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
            CommandUtil.replyCommandError(event)
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