package dev.simpletimer.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * Chromeの拡張機能に必要なIDを取得
 */
object ChromeCommand : SlashCommandManager.SlashCommand("chrome", "Chrome拡張機能に必要なIDを取得する") {
    override fun run(event: SlashCommandInteractionEvent) {
        event.hook.sendMessage("このチャンネルのIDは ${event.channel.idLong} です。").queue()
    }
}