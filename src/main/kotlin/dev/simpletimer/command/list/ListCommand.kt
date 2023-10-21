package dev.simpletimer.command.list

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ListCommand {
    /**
     * 一覧を表示する
     */
    object List : SlashCommandManager.SlashCommand(
        CommandInfoPath.LIST,
        true,
        ListShow,
        ListAddSubCommands.ListAddTimer,
        ListAddSubCommands.ListAddDice,
        ListRemove,
        ListClear,
        ListTargetChannel,
        ListSync,
        ListSyncOff,
        CopyList,
        GetID
    ) {
        override fun run(event: SlashCommandInteractionEvent) {
        }
    }
}