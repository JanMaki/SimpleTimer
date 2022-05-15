package dev.simpletimer.component.button

import dev.simpletimer.dice.bcdice.BCDiceManager
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * ダイスボットの情報を表示
 *
 */
object DiceBotInfoButton : ButtonManager.Button<Byte>("bot_info") {
    override fun run(event: ButtonInteractionEvent) {
        //Embedを作成して送信
        event.hook.sendMessageEmbeds(BCDiceManager.instance.getInfoEmbed(event.channel, event.guild!!)).queue()
    }

    override fun createButton(data: Byte): Button {
        //ボタンを作成して送信
        return Button.secondary(name, Emoji.fromUnicode("❓"))
    }
}