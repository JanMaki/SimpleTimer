package dev.simpletimer.listener

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.util.equalsIgnoreCase
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
        SlashCommandManager.slashCommands.forEach { slashCommand ->
            //名前を確認
            if (slashCommand.name.equalsIgnoreCase(name)) {
                //実行
                slashCommand.autoComplete(event)
            }
        }
    }
}