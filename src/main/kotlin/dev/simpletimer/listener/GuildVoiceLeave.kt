package dev.simpletimer.listener

import dev.simpletimer.util.getAudioPlayer
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * VCを抜けた時に対応を行う
 */
class GuildVoiceLeave : ListenerAdapter() {
    /**
     * VCから抜けたときに呼び出される
     *
     * @param event [GuildVoiceLeaveEvent]
     */
    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        //プレイヤーを取得
        val player = event.guild.getAudioPlayer()

        //接続を確認
        if (!player.isConnected()) return

        //VCの人数を確認
        if (event.channelLeft.members.filter { !it.user.isBot }.size > 1) return

        //切断
        player.disconnect()
    }
}