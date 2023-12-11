package dev.simpletimer.extension

import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

/**
 * [ButtonInteractionEvent]の拡張
 * ボタンが押されたチャンネルで動いている[Timer.Number]のタイマーを取得する
 *
 * @param number 取得するタイマーの[Number]
 * @return [Timer]?
 */
fun ButtonInteractionEvent.getTimer(number: Timer.Number): Timer? {
    return this.channel.asGuildMessageChannel().getTimer(number)
}