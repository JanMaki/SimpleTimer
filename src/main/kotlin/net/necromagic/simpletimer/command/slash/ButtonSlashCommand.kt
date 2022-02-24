package net.necromagic.simpletimer.command.slash

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * ボタンを送信する
 */
object ButtonSlashCommand: SlashCommand("button","タイマーやボタンを開始するボタンを送信します"){
    init {
        addSubcommands(
            SubcommandData("timer", "タイマー")
                .addOptions(OptionData(OptionType.INTEGER, "分", "時間を分単位で").setRequired(true)),
            SubcommandData("dice", "ダイスロール")
                .addOptions(OptionData(OptionType.STRING, "ダイス", "ダイスの内容").setRequired(true))
        )
    }

    override fun run(command: String, event: SlashCommandInteractionEvent) {
        //サブコマンドを取得
        val subCommand = event.subcommandName

        //nullチェック
        if (subCommand == null) {
            replyCommandError(event)
            return
        }

        //ボタンの内容を確認する
        when(subCommand){
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
                val button = Button.primary("timer-${option.asLong}", "⏱開始")
                event.hook.sendMessage("**${option.asLong}分**のタイマーを開始する").addActionRow(button).queue()
            }
            //ダイスのボタン
            "dice" -> {
                //オプションを取得
                val option = event.getOption("ダイス")

                //nullチェック
                if (option == null){
                    replyCommandError(event)
                    return
                }

                //ボタンを送信
                val button = Button.primary("dice-${option.asString}", "🎲振る")
                event.hook.sendMessage("**${option.asString}**を振る").addActionRow(button).queue()
            }
            else -> {
                //エラーメッセージを送信
                replyCommandError(event)
            }
        }
    }
}