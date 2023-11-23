package dev.simpletimer.extension

import dev.simpletimer.SimpleTimer
import dev.simpletimer.audio_player.GuildAudioPlayer
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.database.data.GuildData
import dev.simpletimer.database.transaction.GuildDataTransaction
import net.dv8tion.jda.api.entities.Guild

//Guildを拡張している

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
    return GuildDataTransaction.getGuildData(idLong) ?: GuildData()
}

/**
 * [Guild]の拡張
 * ギルドのデータをセットする
 *
 * @param guildData [GuildData]
 */
fun Guild.setGuildData(guildData: GuildData) {
    GuildDataTransaction.updateGuildData(idLong, guildData)
}

/**
 * [Guild]の拡張
 * 言語のデータを取得する
 *
 * @return [LangData]
 */
fun Guild.getLang(): LangData {
    return SimpleTimer.instance.dataContainer.langDatum[this.getGuildData().lang] ?: LangData()
}