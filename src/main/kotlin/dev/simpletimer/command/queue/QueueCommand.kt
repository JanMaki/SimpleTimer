package dev.simpletimer.command.queue

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object QueueCommand : SlashCommandManager.SlashCommand(
    CommandInfoPath.QUEUE,
    true,
    Add, Show, Remove, Clear
){
    override fun run(event: SlashCommandInteractionEvent) {
    }
}