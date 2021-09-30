package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.SimpleTimer
import net.necromagic.simpletimerKT.util.Log
import net.necromagic.simpletimerKT.util.MessageReply
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
        val user = event.user

        //DMを弾く
        if (event.channel is PrivateChannel) {
            event.reply("*DMでの対応はしていません").queue({}, {})
            return
        }

        val channel = event.textChannel
        val subCommand = event.subcommandName

        val commandManager = SimpleTimer.instance.commandManager

        val messageReply = MessageReply(event)

        event.deferReply().queue({}, {})

        try {
            //ダイスのコマンド
            if (name.equalsIgnoreCase("dice") && subCommand != null) {
                if (subCommand.equalsIgnoreCase("mode")) {
                    commandManager.run(user, channel, listOf("!!dice", "mode"), messageReply)
                }
                if (subCommand.equalsIgnoreCase("bot")) {
                    commandManager.run(user, channel, listOf("!!dice", "bot"), messageReply)
                }
                if (subCommand.equalsIgnoreCase("info")) {
                    commandManager.run(user, channel, listOf("!!dice", "info"), messageReply)
                }
                event.hook.sendMessage("|| ||").complete().delete().queue({}, {})
            }
            //1d100
            if (name.equalsIgnoreCase("1d100")) {
                commandManager.run(user, channel, listOf("!!1d100"), messageReply)
            }
            //1d100のシークレットダイス
            if (name.equalsIgnoreCase("s1d100")) {
                commandManager.run(user, channel, listOf("!!s1d100"), messageReply)
            }
            //タイマーの開始
            if (name.equalsIgnoreCase("timer")) {
                val time = event.getOption("分")?.asLong?.toInt()
                commandManager.run(user, channel, listOf("!!timer", "$time"), messageReply)
                event.hook.sendMessage("|| ||").complete().delete().queue({}, {})
            }
        } catch (e: Exception) {
            Log.sendLog(e.stackTraceToString())
        }
    }

}