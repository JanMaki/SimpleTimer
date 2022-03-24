package dev.simpletimer.data.old_data_converter

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.enum.Mention
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.VoiceChannel
import org.simpleyaml.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Paths


/**
 * 各サーバーのデータなどを処理している
 */
@Deprecated("旧仕様のため使用を廃止")
class ServerConfig : YamlConfiguration() {
    private lateinit var file: File

    init {
        //ファイルを読み込み
        try {
            val parentFile =
                File(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()).toString()).parentFile
            file = File(parentFile, "server_config.yml")
            file.createNewFile()
            loadConfiguration(file)
            load(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * TTSメッセージの内容を取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return [String] メッセージの内容
     */
    fun getTTS(guild: Guild): String {
        return getString("${guild.idLong}.tts_message", "x番目のタイマーが終了しました")
    }

    /**
     * メンションの方式を取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return [Mention] メンションの方式
     */
    fun getMention(guild: Guild): Mention {
        return Mention.valueOf(getString("${guild.idLong}.mention", "VC"))
    }

    /**
     * Cメンション時のターゲットを取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [VoiceChannel]? ボイスチャンネルを返す
     */
    private fun getVCMentionTarget(guild: Guild): VoiceChannel? {
        return guild.getVoiceChannelById(getLong("${guild.idLong}.vc_mention", 0))
    }


    /**
     * メンション時のターゲットのVC一覧を取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [MutableList]<[VoiceChannel]> ロールの一覧を返す
     */
    fun getVCMentionTargetList(guild: Guild): MutableList<VoiceChannel?> {
        //取得
        val list =
            getLongList("${guild.idLong}.vc_mention_list").mapNotNull { guild.getVoiceChannelById(it) }.toMutableList()

        //旧データ
        val voiceChannel = getVCMentionTarget(guild)
        if (voiceChannel != null) {
            list.add(voiceChannel)
        }

        return list.distinct().toMutableList()
    }


    /**
     * メンション時のターゲットのロールを取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [Role]? ロールを返す
     */
    private fun getRoleMentionTarget(guild: Guild): Role? {
        return guild.getRoleById(getLong("${guild.idLong}.role_mention", 0))
    }

    /**
     * メンション時のターゲットのロール一覧を取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [MutableList]<[Role]> ロールの一覧を返す
     */
    fun getRoleMentionTargetList(guild: Guild): MutableList<Role?> {
        val list = getLongList("${guild.idLong}.role_mention_list").mapNotNull { guild.getRoleById(it) }.toMutableList()

        //旧データ
        val role = getRoleMentionTarget(guild)
        if (role != null) {
            list.add(role)
        }


        return list.distinct().toMutableList()
    }

    /**
     * ダイスモードを取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return [DiceMode] ダイスモード
     */
    fun getDiceMode(guild: Guild): DiceMode {
        val modeString = getString("${guild.idLong}.diceMode", "Default")
        return DiceMode.valueOf(modeString)
    }

    /**
     * BCDiceのボットを取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return ダイスボットのID
     */
    fun getDiceBot(guild: Guild): String {
        return getString("${guild.idLong}.diceBot", "DiceBot")
    }

    /**
     * タイマー一覧を取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [MutableMap]<[String],[Int]> タイマーの名前と分のマップ
     */
    fun getTimerList(guild: Guild): MutableMap<String, Int> {
        //文字列のリストを取得
        val list = getStringList("${guild.idLong}.list") ?: mutableListOf()
        //結果用のマップ
        val result = mutableMapOf<String, Int>()
        list.forEach {
            //文字列を':'で分割
            val splitted = it.split(":")
            //':'の前をは名前、後ろは数値に変換して代入
            result[splitted[0]] = splitted[1].toInt()
        }
        return result
    }


    /**
     * 一覧からタイマーを実行するチャンネルを取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [Long] チャンネルのID
     */
    fun getTimerChannelID(guild: Guild): Long {
        return getLong("${guild.idLong}.channel", -1)
    }

    /**
     * リストの同期設定を取得
     *
     * @param guild [Guild] 対象のギルド
     * @return [Boolean] true->on false->off
     */
    fun getListSync(guild: Guild): Boolean {
        return getBoolean("${guild.idLong}.sync", false)
    }

    /**
     * 同期機能の対象を取得
     *
     * @param guild [Guild] 同期先のGuild
     * @return [Guild]? 同期元のGuild
     */
    fun getSyncTarget(guild: Guild): Guild? {
        return SimpleTimer.instance.getGuild(getLong("${guild.id}.sync_target", 0))
    }
}