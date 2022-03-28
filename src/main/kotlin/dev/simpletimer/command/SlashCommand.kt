package dev.simpletimer.command

import dev.simpletimer.extension.sendMessage
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.internal.interactions.CommandDataImpl

/**
 * スラッシュコマンドの親
 *
 * @param name コマンド名
 * @param description コマンドの説明
 */
abstract class SlashCommand(name: String, description: String, val beforeReply: Boolean = true) :
    CommandDataImpl(name, description) {

    init {
        isDefaultEnabled = true
    }

    /**
     * コマンドを実行する
     *
     * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
     */
    abstract fun run(event: SlashCommandInteractionEvent)

    /**
     * 自動補完をする
     *
     * @param event [CommandAutoCompleteInteractionEvent] 自動補完のイベント
     */
    open fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
        //デフォルトでは何もしない
    }

    companion object {
        /**
         * コマンドエラーを送信する
         *
         * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
         */
        fun replyCommandError(event: SlashCommandInteractionEvent) {
            event.hook.sendMessage("*コマンドエラー", true).queue({}){
                event.reply("*コマンドエラー").queue()
            }
        }
    }
}