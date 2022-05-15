package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

/**
 * ボタンのマネージャー
 *
 */
class ButtonManager {
    //ボタンの一覧
    val buttons = mutableSetOf<Button<*>>().apply {
        //ボタンを追加
        addAll(
            arrayOf(
                AddTimerButton,
                CommunityLinkButton,
                DiceButton,
                DeleteMessageButton,
                DiceBotInfoButton,
                DiceBotPageButton.NextButton,
                DiceBotPageButton.BackButton,
                FinishButton,
                RestartButton,
                StopButton,
                TimerButton
            )
        )
    }

    /**
     * ボタンの親
     *
     * @property name 識別に使う名前
     * @property deferReply あらかじめbeforeReplyを実行するかどうか
     */
    abstract class Button<T>(val name: String, val deferReply: Boolean = true) {
        /**
         * ボタンを実行する
         *
         * @param event [ButtonInteractionEvent] ボタンを押したイベント
         */
        abstract fun run(event: ButtonInteractionEvent)

        /**
         * ボタンを作成する
         *
         * @param data ボタンに何かデータつける時に使う
         * @param langData [LangData]言語のデータ
         * @return 作成した[Button]
         */
        abstract fun createButton(
            data: T,
            langData: LangData
        ): net.dv8tion.jda.api.interactions.components.buttons.Button
    }
}