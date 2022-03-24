package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import net.dv8tion.jda.api.entities.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * VCへ入った時に対応を行う
 */
class GuildVoiceJoin : ListenerAdapter() {
    /**
     * VCに入ったときに呼び出される
     *
     * @param event [GuildVoiceJoinEvent]
     */
    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        //IDを確認
        if (event.member.user.idLong != SimpleTimer.instance.shards.first().selfUser.idLong) return

        //チャンネルを取得
        val channel = event.channelJoined
        //ステージチャンネルかを確認
        if (channel !is StageChannel) return

        //リクエストを送る
        channel.requestToSpeak().queue()
    }
}