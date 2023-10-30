package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.extension.equalsIgnoreCase
import dev.simpletimer.util.Log
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * スラッシュコマンドに対応をするクラス
 */
class SlashCommandInteraction : ListenerAdapter() {
    /**
     * スラッシュコマンドを実行した時に呼び出される
     *
     * @param event [SlashCommandInteractionEvent] イベント
     */
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        //各種値を取得
        val name = event.name

        //DMを弾く
        if (event.channel is PrivateChannel) {
            event.reply("*DMでの対応はしていません").queue()
            return
        }

        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            //権限が不足しているメッセージを送信する
            event.replyEmbeds(SimpleTimer.instance.getErrorEmbed(event.guildChannel)).queue()
            return
        }


        try {
            //スラッシュコマンドを実行する
            SlashCommandManager.slashCommands.firstOrNull { it.name.equalsIgnoreCase(name) }?.let { slashCommand ->
                //サブコマンドがあるかを確認
                event.subcommandName?.let { subcommandName ->
                    //サブコマンドを探して実行
                    slashCommand.subcommands.firstOrNull { it.subCommandData.name.equalsIgnoreCase(subcommandName) }
                        ?.let { subcommand ->
                            //考え中をするかを確認
                            if (subcommand.deferReply) {
                                //通常のコマンドを実行
                                event.deferReply().queue()
                            }
                            //サブコマンドを実行
                            subcommand.run(event)
                            return
                        }
                }

                //通常のコマンドを実行
                //考え中をするかを確認
                if (slashCommand.deferReply) {
                    //考え中を出す
                    event.deferReply().queue()
                }
                slashCommand.run(event)
            }
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)) {
                return
            }
            Log.sendLog(e.stackTraceToString(), true)
        }
    }

}