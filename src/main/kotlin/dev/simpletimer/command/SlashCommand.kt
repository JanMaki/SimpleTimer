package dev.simpletimer.command

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
     * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
     */
    abstract fun run(event: SlashCommandInteractionEvent)

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