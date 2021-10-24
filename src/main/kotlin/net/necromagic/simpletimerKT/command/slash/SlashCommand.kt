package net.necromagic.simpletimerKT.command.slash

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

/**
 * スラッシュコマンドの親
 *
 * @param name コマンド名
 * @param description コマンドの説明
 */
abstract class SlashCommand(name: String, description: String) : CommandData(name, description) {
    /**
     * コマンドを実行する
     *
     * @param command [String] コマンドの文字列
     * @param event [SlashCommandEvent] スラッシュコマンドのイベント
     */
    abstract fun run(command: String, event: SlashCommandEvent)

    companion object {
        /**
         * コマンドエラーを送信する
         *
         * @param event [SlashCommandEvent] スラッシュコマンドのイベント
         */
        fun replyCommandError(event: SlashCommandEvent) {
            event.hook.sendMessage("*コマンドエラー").queue()
        }
    }
}