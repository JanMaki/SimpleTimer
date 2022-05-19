package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * ギルドを抜けたときの処理をするクラス
 *
 */
class GuildLeave : ListenerAdapter() {
    /**
     * ギルドを抜けた時に呼び出される
     *
     * @param event [GuildLeaveEvent]
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        SimpleTimer.instance.audioManager.deleteAudioPlayer(event.guild)
    }
}