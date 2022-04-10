package dev.simpletimer.command

import dev.simpletimer.component.modal.QueueModal
import dev.simpletimer.component.modal.YesOrNoModal
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.extension.sendMessage
import dev.simpletimer.extension.sendMessageEmbeds
import dev.simpletimer.timer.Timer
import dev.simpletimer.timer.TimerQueue
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class QueueCommands {
    /**
     * キューを追加する
     *
     */
    object Queue : SlashCommandManager.SlashCommand("queue", "タイマーをキューに追加をする", false) {
        init {
            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "キューを追加するタイマー").addChoices(
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
            //タイマーの番号を取得
            val number = Timer.Number.getNumber(timerOption?.asInt ?: 1) ?: Timer.Number.FIRST

            //Modalを返信
            event.replyModal(QueueModal.createModal(number)).queue()
        }
    }

    /**
     * キューを表示する
     *
     */
    object Show : SlashCommandManager.SlashCommand("queue_show", "キューを確認する") {
        init {
            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "確認をするタイマー").addChoices(
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
            //タイマーの番号を取得
            val number = Timer.Number.getNumber(timerOption?.asInt ?: 1) ?: Timer.Number.FIRST

            event.hook.sendMessageEmbeds(
                TimerQueue.getTimerQueue(event.guild!!, event.guildChannel, number).getQueueEmbed(),
                true
            ).queue()
        }
    }

    /**
     * キューを削除する
     *
     */
    object Remove : SlashCommandManager.SlashCommand("queue_remove", "キューからタイマーを削除する") {
        init {
            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "キューを削除するタイマー").addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                ).setRequired(true),
                OptionData(OptionType.INTEGER, "番号", "削除するタイマーのキュー番号").setRequired(true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val timerOption = event.getOption("タイマー")
            val indexOption = event.getOption("番号")
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

            //キューがあるかを確認する
            if (queue.getQueue().size < index + 1) {
                event.hook.sendMessage("該当のキューが見つかりません", true).queue()
                return
            }
            //削除
            queue.removeQueueIndex(index)

            //メッセージを送信
            event.hook.sendMessage("${number.number}番目のタイマーのキューを削除しました").queue()
        }
    }

    /**
     * キューをクリアする
     *
     */
    object Clear : SlashCommandManager.SlashCommand("queue_clear", "キュー全て削除する", deferReply = false) {
        init {
            addOptions(
                OptionData(OptionType.INTEGER, "タイマー", "キューを削除するタイマー").addChoices(
                    Command.Choice("1番目のタイマー", 1),
                    Command.Choice("2番目のタイマー", 2),
                    Command.Choice("3番目のタイマー", 3),
                    Command.Choice("4番目のタイマー", 4)
                ).setRequired(true),
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
                it.hook.sendMessage("${number.number}番目のタイマーのキューをすべて削除しました").queue()
            }
            //確認のModalでNoを選択したときの処理
            val noAction = YesOrNoModal.Action {
                it.hook.sendEmpty()
            }

            //Modalを作成して返す
            event.replyModal(YesOrNoModal.createModal(YesOrNoModal.Data(event.user.idLong, yesAction, noAction)))
                .queue()
        }
    }
}