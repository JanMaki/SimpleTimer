package dev.simpletimer.command

import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * 拡張機能に必要なIDを取得
 */
object ChromeCommand : SlashCommandManager.SlashCommand(CommandInfoPath.EXTENSION) {
    override fun run(event: SlashCommandInteractionEvent) {
        event.hook.sendMessage(event.guild!!.getLang().command.chrome.output.langFormat(event.channel.idLong)).queue()
    }
}