package dev.simpletimer.command.queue

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.component.modal.QueueModal
import dev.simpletimer.component.modal.YesOrNoModal
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.getOption
import dev.simpletimer.extension.langFormat
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import dev.simpletimer.timer.TimerQueue
import dev.simpletimer.util.CommandUtil.createChoice
import dev.simpletimer.util.CommandUtil.createOptionData
import dev.simpletimer.util.CommandUtil.replyCommandError
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * キューを追加する
 *
 */
object Add : SlashCommandManager.SubCommand(CommandInfoPath.QUEUE_ADD, false) {
    init {
        addOptions(
            createOptionData(OptionType.INTEGER, CommandInfoPath.QUEUE_OPT_ADD_TIMER).addChoices(
                createChoice(CommandInfoPath.ONE, 1),
                createChoice(CommandInfoPath.TWO, 2),
                createChoice(CommandInfoPath.THREE, 3),
                createChoice(CommandInfoPath.FOUR, 4)
            )
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val timerOption = event.getOption(CommandInfoPath.QUEUE_OPT_ADD_TIMER)
        //タイマーの番号を取得
        val number = Timer.Number.getNumber(timerOption?.asInt ?: 1) ?: Timer.Number.FIRST

        //Modalを返信
        event.replyModal(QueueModal.createModal(number, event.guild!!.getLang())).queue()
    }
}

/**
 * キューを表示する
 *
 */
object Show : SlashCommandManager.SubCommand(CommandInfoPath.QUEUE_SHOW) {
    init {
        addOptions(
            createOptionData(OptionType.INTEGER, CommandInfoPath.QUEUE_OPT_CHECK_TIMER).addChoices(
                createChoice(CommandInfoPath.ONE, 1),
                createChoice(CommandInfoPath.TWO, 2),
                createChoice(CommandInfoPath.THREE, 3),
                createChoice(CommandInfoPath.FOUR, 4)
            )
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val timerOption = event.getOption(CommandInfoPath.QUEUE_OPT_CHECK_TIMER)
        //タイマーの番号を取得
        val number = Timer.Number.getNumber(timerOption?.asInt ?: 1) ?: Timer.Number.FIRST

        event.hook.sendMessageEmbeds(
            TimerQueue.getTimerQueue(event.guild!!, event.guildChannel, number)
                .getQueueEmbed(event.guild!!.getLang())
        ).queue()
    }
}

/**
 * キューを削除する
 *
 */
object Remove : SlashCommandManager.SubCommand(CommandInfoPath.QUEUE_REMOVE) {
    init {
        addOptions(
            createOptionData(OptionType.INTEGER, CommandInfoPath.QUEUE_OPT_REMOVE_TIMER).addChoices(
                createChoice(CommandInfoPath.ONE, 1),
                createChoice(CommandInfoPath.TWO, 2),
                createChoice(CommandInfoPath.THREE, 3),
                createChoice(CommandInfoPath.FOUR, 4)
            ).setRequired(true),
            createOptionData(OptionType.INTEGER, CommandInfoPath.QUEUE_OPT_NUMBER).setRequired(true)
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val timerOption = event.getOption(CommandInfoPath.QUEUE_OPT_REMOVE_TIMER)
        val indexOption = event.getOption(CommandInfoPath.QUEUE_OPT_NUMBER)
        //nullチェック
        if (timerOption == null || indexOption == null) {
            replyCommandError(event)
            return
        }

        //タイマーの番号を取得
        val number = Timer.Number.getNumber(timerOption.asInt)
        if (number == null) {
            replyCommandError(event)
            return
        }

        //インデックスを取得
        val index = indexOption.asInt - 1

        //キューを取得
        val queue = TimerQueue.getTimerQueue(event.guild!!, event.guildChannel, number)

        //言語のデータ
        val langData = event.guild!!.getLang()

        //キューがあるかを確認する
        if (queue.getQueue().size < index + 1) {
            event.hook.sendMessage(langData.command.queue.queueNotFount).queue()
            return
        }
        //削除
        queue.removeQueueIndex(index)

        //メッセージを送信
        event.hook.sendMessage(langData.command.queue.remove.langFormat(number.number)).queue()
    }
}

/**
 * キューをクリアする
 *
 */
object Clear : SlashCommandManager.SubCommand(CommandInfoPath.QUEUE_CLEAR, deferReply = false) {
    init {
        addOptions(
            createOptionData(OptionType.INTEGER, CommandInfoPath.QUEUE_OPT_CLEAR_TIMER).addChoices(
                createChoice(CommandInfoPath.ONE, 1),
                createChoice(CommandInfoPath.TWO, 2),
                createChoice(CommandInfoPath.THREE, 3),
                createChoice(CommandInfoPath.FOUR, 4)
            ).setRequired(true),
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val timerOption = event.getOption(CommandInfoPath.QUEUE_OPT_CLEAR_TIMER)
        //nullチェック
        if (timerOption == null) {
            replyCommandError(event)
            return
        }

        //タイマーの番号を取得
        val number = Timer.Number.getNumber(timerOption.asInt)
        if (number == null) {
            replyCommandError(event)
            return
        }

        //確認のModalでYesを選択したときの処理
        val yesAction = YesOrNoModal.Action {
            //キューを取得してクリアする
            TimerQueue.getTimerQueue(event.guild!!, event.guildChannel, number).clearQueue()

            //メッセージを送信
            it.hook.sendMessage(event.guild!!.getLang().command.queue.clear.langFormat(number.number)).queue()
        }
        //確認のModalでNoを選択したときの処理
        val noAction = YesOrNoModal.Action {
            it.hook.sendEmpty()
        }

        //Modalを作成して返す
        event.replyModal(
            YesOrNoModal.createModal(
                YesOrNoModal.Data(event.user.idLong, yesAction, noAction),
                event.guild!!.getLang()
            )
        )
            .queue()
    }
}