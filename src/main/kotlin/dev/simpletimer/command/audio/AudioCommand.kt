package dev.simpletimer.command.audio

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * オーディオのコマンド
 *
 * @constructor Create empty Audio command
 */
object AudioCommand : SlashCommandManager.SlashCommand(
    CommandInfoPath.AUDIO,
    deferReply = true,
    Connect, DisConnect, Listen, Change, AudioList
) {
    override fun run(event: SlashCommandInteractionEvent) {
    }
}