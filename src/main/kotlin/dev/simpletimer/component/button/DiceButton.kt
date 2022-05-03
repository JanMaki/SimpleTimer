package dev.simpletimer.component.button

import dev.simpletimer.dice.Dice
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * ダイスを振るボタン
 *
 */
object DiceButton : ButtonManager.Button<String>("dice") {
    override fun run(event: ButtonInteractionEvent) {
        val buttonID = event.button.id ?: return

        //ダイスのコマンドを取得
        val value = buttonID.replace("${name}:", "")

        //ダイスを振る
        Dice().roll(event, value, event.user)
    }

    override fun createButton(data: String): Button {
        return Button.primary("${name}:$data", "振る").withEmoji(Emoji.fromUnicode("\uD83C\uDFB2"))
    }
}