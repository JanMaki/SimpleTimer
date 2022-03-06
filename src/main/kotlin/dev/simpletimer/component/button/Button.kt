package dev.simpletimer.component.button

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

/**
 * ボタンの親
 *
 */
interface Button {
    /**
     * ボタンを実行する
     *
     * @param event [ButtonInteractionEvent] ボタンを押したイベント
     */
    fun run(event: ButtonInteractionEvent)

    /**
     * ボタンを作成する
     *
     * @param data ボタンに何かデータつける時に使う
     * @return 作成した[Button]
     */
    fun createButton(data: String = ""): net.dv8tion.jda.api.interactions.components.buttons.Button
}