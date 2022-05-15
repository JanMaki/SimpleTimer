package dev.simpletimer.command

import dev.simpletimer.component.button.DiceButton
import dev.simpletimer.component.modal.TimerButtonModal
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

/**
 * ボタンを送信する
 */
object ButtonCommand : SlashCommandManager.SlashCommand("button", "タイマーやボタンを開始するボタンを送信します", false) {
    init {
        addSubcommands(
            SubcommandData("timer", "タイマー"),
            SubcommandData("dice", "ダイスロール")
                .addOptions(OptionData(OptionType.STRING, "ダイス", "ダイスの内容").setRequired(true))
        )
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //サブコマンドを取得
        val subCommand = event.subcommandName

        //nullチェック
        if (subCommand == null) {
            //とりあえず待たせる
            event.deferReply().queue()
            //エラーメッセージを送信
            replyCommandError(event)
            return
        }

        //ボタンの内容を確認する
        when (subCommand) {
            //タイマーのボタン
            "timer" -> {
                //Modalを作成して返す
                event.replyModal(TimerButtonModal.createModal(0, event.guild!!.getLang())).queue()
            }
            //ダイスのボタン
            "dice" -> {
                //とりあえず待たせる
                event.deferReply().queue()

                //オプションを取得
                val option = event.getOption("ダイス")

                //nullチェック
                if (option == null) {
                    replyCommandError(event)
                    return
                }

                //ダイスの内容を取得
                val dice = option.asString

                //言語のデータ
                val langData = event.guild?.getLang() ?: return

                //文字数制限
                if (dice.length >= 30) {
                    event.hook.sendMessage(langData.command.button.longLengthWarning).queue()
                    return
                }

                //ボタンを送信
                event.hook.sendMessage(langData.command.button.roll.langFormat("**${option.asString}**"))
                    .addActionRow(DiceButton.createButton(option.asString, event.guild!!.getLang()))
                    .queue()
            }
            else -> {
                //とりあえず待たせる
                event.deferReply().queue()
                //エラーメッセージを送信
                replyCommandError(event)
            }
        }
    }
}