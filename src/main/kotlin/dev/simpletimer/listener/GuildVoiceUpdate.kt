package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.extension.getAudioPlayer
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * VCの更新に対して応答する
 *
 */
class GuildVoiceUpdate : ListenerAdapter() {
    /**
     * VCが更新されたときに呼び出される
     *
     * @param event [GuildVoiceUpdateEvent]
     */
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        //Join時
        val channelJoin = event.channelJoined
        if (channelJoin != null){
            //IDを確認
            if (event.member.user.idLong != SimpleTimer.instance.shards.first().selfUser.idLong) return

            //チャンネルを取得
            val channel = event.channelJoined
            //ステージチャンネルかを確認
            if (channel !is StageChannel) return

            //リクエストを送る
            channel.requestToSpeak().queue()
        }

        //Left時
        val channelLeft = event.channelLeft
        if (channelLeft != null){
            //プレイヤーを取得
            val player = event.guild.getAudioPlayer()

            //接続を確認
            if (!player.isConnected()) return

            //VCの人数を確認
            if (channelLeft.members.any { !it.user.isBot }) return

            //切断
            player.disconnect()
        }
    }
}