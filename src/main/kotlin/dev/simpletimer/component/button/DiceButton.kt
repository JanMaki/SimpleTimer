package dev.simpletimer.component.button

import dev.simpletimer.dice.Dice
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

/**
 * ダイスを振るボタン
 *
 */
object DiceButton : dev.simpletimer.component.button.Button {
    override fun run(event: ButtonInteractionEvent) {
        val buttonID = event.button.id ?: return

        //ダイスのボタン
        if (buttonID.contains("dice")) {
            //ダイスのコマンドを取得
            val value = buttonID.replace("dice-", "")

            //ダイスを振る
            Dice().roll(event, value, event.user)
        }
    }

    override fun createButton(data: String): Button {
        return Button.primary("dice-$data", "🎲振る")
    }
}