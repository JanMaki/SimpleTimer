package dev.simpletimer.audio_player

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Guild

/**
 * オーディオプレイヤーのマネージャー
 *
 */
class AudioPlayerManager {
    //各ギルドのオーディオプレイヤー
    private val audioPlayers = mutableMapOf<Long, GuildAudioPlayer>()

    //オーディオ再生のマネージャー
    val playerManager = DefaultAudioPlayerManager().apply {
        //ソースのマネージャーに追加
        AudioSourceManagers.registerRemoteSources(this)
    }

    /**
     * ギルドのオーディオプレイヤーを取得する
     *
     * @param guild 対象の[Guild]
     * @return [GuildAudioPlayer]
     */
    fun getAudioPlayer(guild: Guild): GuildAudioPlayer {
        return audioPlayers.getOrPut(guild.idLong) { GuildAudioPlayer(guild) }
    }
}