package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.button.ButtonManager
import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.extension.equalsIgnoreCase
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

        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            //権限が不足しているメッセージを送信する
            event.replyEmbeds(SimpleTimer.instance.getErrorEmbed(event.guildChannel)).queue()
            return
        }

        //ボタンのIDを取得
        val buttonID = event.button.id ?: return

        //すべてのボタン
        buttonManager.buttons.filter { buttonID.split(":")[0].equalsIgnoreCase(it.name) }.forEach { button ->
            //考え中をするかを確認
            if (button.beforeReply) {
                //考え中を出す
                event.deferReply().queue()
            }

            //ボタンを実行
            button.run(event)
        }
    }
}