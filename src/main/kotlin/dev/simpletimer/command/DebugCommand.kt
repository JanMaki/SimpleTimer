package dev.simpletimer.command

import dev.simpletimer.component.modal.DebugModal
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * デバッグのメニューを開く
 */
object DebugCommand : SlashCommand("debug", "開発用機能", false) {
    override fun run(event: SlashCommandInteractionEvent) {
        //デバッグのモーダルを作成して返す
        event.replyModal(DebugModal.createModal(0)).queue()
    }
}