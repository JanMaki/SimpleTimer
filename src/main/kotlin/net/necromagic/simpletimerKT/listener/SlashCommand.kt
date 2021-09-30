package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.command.slash.SlashCommandManager
import net.necromagic.simpletimerKT.util.Log
import net.necromagic.simpletimerKT.util.SendMessage
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

        //権限を確認
        val permissions = event.guild!!.selfMember.getPermissions(event.textChannel)
        if (!permissions.contains(Permission.ADMINISTRATOR)) {
            if (!(
                        permissions.contains(Permission.VIEW_CHANNEL) &&
                                permissions.contains(Permission.MESSAGE_SEND) &&
                                permissions.contains(Permission.MESSAGE_TTS) &&
                                permissions.contains(Permission.MESSAGE_EMBED_LINKS) &&
                                permissions.contains(Permission.MESSAGE_HISTORY) &&
                                permissions.contains(Permission.MESSAGE_EXT_EMOJI))
            ) {
                event.deferReply().queue()
                event.hook.sendMessageEmbeds(SendMessage.errorEmbed).queue()
                return
            }
        }

        try {
            SlashCommandManager.slashCommands.forEach { slashCommand ->
                if (slashCommand.name.equalsIgnoreCase(name)) {
                    slashCommand.run(name, event)
                }
            }
        }catch (e: Exception){
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)){
                return
            }
            Log.sendLog(e.stackTraceToString())
        }
    }

}