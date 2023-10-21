package dev.simpletimer.command.timer

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.getOption
import dev.simpletimer.extension.langFormat
import dev.simpletimer.util.CommandUtil
import dev.simpletimer.util.CommandUtil.createChoice
import dev.simpletimer.util.CommandUtil.createOptionData
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType


/**
 * TTSの設定を行う
 */
object TTSTiming : SlashCommandManager.SubCommand(CommandInfoPath.TTS_TIMING) {
    init {
        addOptions(
            createOptionData(OptionType.INTEGER, CommandInfoPath.TIMER_OPT_TYPE, true)
                .addChoices(
                    createChoice(CommandInfoPath.TIMER_SC_ZERO, 0),
                    createChoice(CommandInfoPath.TIMER_SC_ONE, 1),
                    createChoice(CommandInfoPath.TIMER_SC_TWO, 2),
                    createChoice(CommandInfoPath.TIMER_SC_THREE, 3),
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

        //ttsのタイミングを取得
        val timing = when (timingOption.asLong.toInt()) {
            1 -> NoticeTiming.LV1
            2 -> NoticeTiming.LV2
            3 -> NoticeTiming.LV3
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
object FinishTTS : SlashCommandManager.SubCommand(CommandInfoPath.TTS_FINISH_MESSAGE) {
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