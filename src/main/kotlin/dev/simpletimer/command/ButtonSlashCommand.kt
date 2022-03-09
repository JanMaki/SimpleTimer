package dev.simpletimer.command

import dev.simpletimer.component.button.DiceButton
import dev.simpletimer.component.button.TimerButton
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

/**
 * ボタンを送信する
 */
object ButtonSlashCommand : SlashCommand("button", "タイマーやボタンを開始するボタンを送信します") {
    init {
        addSubcommands(
            SubcommandData("timer", "タイマー")
                .addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で").setRequired(true)),
            SubcommandData("dice", "ダイスロール")
                .addOptions(OptionData(OptionType.STRING, "ダイス", "ダイスの内容").setRequired(true))
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

        //ボタンの内容を確認する
        when (subCommand) {
            //タイマーのボタン
            "timer" -> {
                //オプションを取得
                val option = event.getOption("分")

                //nullチェック
                if (option == null) {
                    replyCommandError(event)
                    return
                }

                //ボタンを送信
                event.hook.sendMessage("**${option.asLong}分**のタイマーを開始する")
                    .addActionRow(TimerButton.createButton(option.asLong.toString()))
                    .queue({}, {})
            }
            //ダイスのボタン
            "dice" -> {
                //オプションを取得
                val option = event.getOption("ダイス")

                //nullチェック
                if (option == null) {
                    replyCommandError(event)
                    return
                }

                //ボタンを送信
                event.hook.sendMessage("**${option.asString}**を振る")
                    .addActionRow(DiceButton.createButton(option.asString))
                    .queue({}, {})
            }
            else -> {
                //エラーメッセージを送信
                replyCommandError(event)
            }
        }
    }
}