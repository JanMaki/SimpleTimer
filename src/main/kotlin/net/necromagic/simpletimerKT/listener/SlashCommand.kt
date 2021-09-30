package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.command.slash.SlashCommandManager
import net.necromagic.simpletimerKT.util.equalsIgnoreCase

/**
 * スラッシュコマンドに対応をするクラス
 */
class SlashCommand : ListenerAdapter() {

    /**
     * スラッシュコマンドを実行した時に呼び出される
     *
     * @param event [SlashCommandEvent] イベント
     */
    override fun onSlashCommand(event: SlashCommandEvent) {
        //各種値を取得
        val name = event.name

        //DMを弾く
        if (event.channel is PrivateChannel) {
            event.reply("*DMでの対応はしていません").queue({}, {})
            return
        }

        SlashCommandManager.slashCommands.forEach { slashCommand ->
            if (slashCommand.name.equalsIgnoreCase(name)) {
                slashCommand.run(name, event)
            }
        }
    }

}