package dev.simpletimer.listener

import dev.simpletimer.component.button.ButtonManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * ボタンに対応をするクラス
 *
 * @constructor Create empty Button click
 */
class ButtonInteraction : ListenerAdapter() {
    private val buttonManager = ButtonManager()

    /**
     * ボタンを実行した時に呼び出される
     *
     * @param event [ButtonInteractionEvent] イベント
     */
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        super.onButtonInteraction(event)
        event.deferReply().queue({}, {})

        //ボタンのIDを取得
        val buttonID = event.button.id ?: return

        //すべてのボタン
        buttonManager.buttons.filter { it.name.startsWith(buttonID) }.forEach { button ->
            //ボタンを実行
            button.run(event)
        }
    }
}