package dev.simpletimer.util

import dev.simpletimer.SimpleTimer
import dev.simpletimer.audio_player.GuildAudioPlayer
import dev.simpletimer.data.guild.GuildData
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

/**
 * [Guild]の拡張
 * ギルドのデータを取得する
 *
 * @return [GuildData]
 */
fun Guild.getGuildData(): GuildData {
    return SimpleTimer.instance.dataContainer.getGuildData(this)
}