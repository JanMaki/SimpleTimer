package dev.simpletimer.util

import dev.simpletimer.SimpleTimer
import dev.simpletimer.audio_player.GuildAudioPlayer
import net.dv8tion.jda.api.entities.Guild

/**
 * オーディオプレイヤーを取得
 *
 * @return [GuildAudioPlayer]
 */
fun Guild.getAudioPlayer(): GuildAudioPlayer {
    //オーディオのマネージャーを取得
    val audioManager = SimpleTimer.instance.audioManager

    //オーディオプレイヤーを取得して返す
    return audioManager.getAudioPlayer(this)
}