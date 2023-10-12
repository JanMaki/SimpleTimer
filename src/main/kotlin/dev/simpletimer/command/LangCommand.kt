package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

/**
 * 言語を変更するコマンド
 */
object LangCommand : SlashCommandManager.SlashCommand(CommandInfoPath.LANG) {
    init {
        //すべての言語を確認
        Lang.entries.forEach {
            //サブコマンドを追加
            addSubcommands(SubcommandData(it.name.lowercase(), it.displayName))
        }
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //サブコマンドの名前を取得
        val subCommandName = event.subcommandName

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
            SimpleTimer.instance.dataContainer.langDatum[lang]?.command?.lang?.change?.langFormat("**${lang.displayName}**")
                ?: ""
        ).queue()
    }
}