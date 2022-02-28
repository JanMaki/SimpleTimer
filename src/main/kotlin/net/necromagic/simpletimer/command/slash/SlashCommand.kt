package net.necromagic.simpletimer.command.slash

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.internal.interactions.CommandDataImpl

/**
 * スラッシュコマンドの親
 *
 * @param name コマンド名
 * @param description コマンドの説明
 */
abstract class SlashCommand(name: String, description: String) : CommandDataImpl(name, description) {
    /**
     * コマンドを実行する
     *
     * @param command [String] コマンドの文字列
     * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
     */
    abstract fun run(command: String, event: SlashCommandInteractionEvent)

    companion object {
        /**
         * コマンドエラーを送信する
         *
         * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
         */
        fun replyCommandError(event: SlashCommandInteractionEvent) {
            event.hook.sendMessage("*コマンドエラー").queue({}, {})
        }
    }
}