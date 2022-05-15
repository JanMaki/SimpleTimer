package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

/**
 * 言語を変更するコマンド
 */
object LangCommand: SlashCommandManager.SlashCommand("lang", "言語を変更する") {
    init {
        //すべての言語を確認
        Lang.values().forEach {
            //サブコマンドを追加
            addSubcommands(SubcommandData(it.name.lowercase(), it.displayName))
        }
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //サブコマンドの名前を取得
        val subCommandName =  event.subcommandName

        //nullチェック
        if (subCommandName == null) {
            replyCommandError(event)
            return
        }

        //言語を取得
        val lang = Lang.valueOf(subCommandName.uppercase())

        //言語を保存
        event.guild!!.getGuildData().lang = lang
        SimpleTimer.instance.dataContainer.saveGuildsData(event.guild!!)

        //メッセージを送信
        event.hook.sendMessage(
            SimpleTimer.instance.dataContainer.langs[lang]?.command?.lang?.change?.langFormat("**${lang.displayName}**")
                ?: ""
        ).queue()
    }
}