package dev.simpletimer.listener

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.extension.equalsIgnoreCase
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * コマンドの自動補完対応をするクラス
 *
 */
class CommandAutoCompleteInteraction : ListenerAdapter() {
    /**
     * コマンドのオプション入力時に呼び出される
     *
     * @param event [CommandAutoCompleteInteractionEvent] イベント
     */
    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        super.onCommandAutoCompleteInteraction(event)

        //名前を取得
        val name = event.name

        //スラッシュコマンドを実行する
        SlashCommandManager.slashCommands.firstOrNull { it.name.equalsIgnoreCase(name) }?.let { slashCommand ->
            //サブコマンドがあるかを
            event.subcommandName?.let { subcommandName ->
                //サブコマンドで実装している自動補完
                slashCommand.subcommands.firstOrNull { it.subCommandData.name.equalsIgnoreCase(subcommandName) }?.let {
                    it.autoComplete(event)
                    return
                }
            }

            //通常のコマンドの自動補完
            slashCommand.autoComplete(event)
        }
    }
}