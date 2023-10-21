package dev.simpletimer.command.timer

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.component.modal.AddTimerModal
import dev.simpletimer.component.modal.StartTimerModal
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.getOption
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import dev.simpletimer.util.CommandUtil.createChoice
import dev.simpletimer.util.CommandUtil.createOptionData
import dev.simpletimer.util.CommandUtil.replyCommandError
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
     * TTSの設定
     */
    object TTS : SlashCommandManager.SlashCommand(CommandInfoPath.TTS, true, TTSTiming, FinishTTS) {
        override fun run(event: SlashCommandInteractionEvent) {
        }
    }

    /**
     * メンションの設定
     *
     */
    object Mention : SlashCommandManager.SlashCommand(
        CommandInfoPath.MENTION,
        true,
        MentionTiming,
        MentionType,
        ShowRoleMentionTarget,
        AddRoleMentionTarget,
        RemoveRoleMentionTarget,
        ShowVCMentionTarget,
        AddVCMentionTarget,
        RemoveVCMentionTarget
    ) {
        override fun run(event: SlashCommandInteractionEvent) {
        }
    }
}