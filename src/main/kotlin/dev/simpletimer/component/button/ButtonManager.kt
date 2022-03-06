package dev.simpletimer.component.button

/**
 * ボタンのマネージャー
 *
 * @constructor Create empty Button manager
 */
class ButtonManager {
    //ボタンの一覧
    val buttons = mutableSetOf<Button>().apply {
        //ボタンを追加
        addAll(
            arrayOf(
                DiceButton,
                TimerButton
            )
        )
    }
}