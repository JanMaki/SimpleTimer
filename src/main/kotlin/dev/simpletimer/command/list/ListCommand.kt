package dev.simpletimer.command.list

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.list.ListMenu
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ListCommand {
    /**
     * 一覧を表示する
     */
    object List : SlashCommandManager.SlashCommand(
        CommandInfoPath.LIST,
        true,
        /*todo ListAdd,*/ListRemove, ListClear, ListTargetChannel, /*todo SyncList,*/CopyList, GetID
    ) {
        override fun run(event: SlashCommandInteractionEvent) {
            //一覧を送信する
            ListMenu.sendList(event)
        }
    }
}