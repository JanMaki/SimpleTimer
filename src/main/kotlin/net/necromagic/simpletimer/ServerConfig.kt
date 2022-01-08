package net.necromagic.simpletimer

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.VoiceChannel
import net.necromagic.simpletimer.ServerConfig.TTSTiming.*
import net.necromagic.simpletimer.util.equalsIgnoreCase
import org.simpleyaml.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Paths


/**
 * 各サーバーのデータなどを処理している
 *
 */
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
     * TTSのレベルの確認を行う
     * @param guild [Guild] 該当のギルド
     * @param timing [TTSTiming] 確認を行うタイミング
     * @return [Boolean] 結果を返す
     */
    fun checkTTS(guild: Guild, timing: TTSTiming): Boolean {
        val guildTiming = TTSTiming.fromString(getString("${guild.id}.tts", "LV0"))
        return guildTiming.priority >= timing.priority
    }

    /**
     * TTSのタイミングを設定する
     *
     * @param guild [Guild] 該当のギルド
     * @param timing [TTSTiming] TTSのタイミング
     */
    fun setTTS(guild: Guild, timing: TTSTiming) {
        set("${guild.idLong}.tts", timing.toString())
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
     * TTSメッセージの内容を設定する
     *
     * @param guild [Guild] 該当のギルド
     * @param string [String] メッセージの内容を渡す
     */
    fun setFinishTTS(guild: Guild, string: String) {
        set("${guild.idLong}.tts_message", string)
    }

    /**
     * メンションの方式を取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return [Mention] メンションの方式
     */
    fun getMention(guild: Guild): Mention {
        return Mention.fromString(getString("${guild.idLong}.mention", "VC"))
    }


    /**
     * メンションの内容を取得する
     *
     * @param guild [Guild] 該当のギルド
     * @param mention [Mention] メンションの方式を渡す
     */
    fun setMention(guild: Guild, mention: Mention) {
        set("${guild.idLong}.mention", mention.toString())
    }


    /**
     * Cメンション時のターゲットを取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [VoiceChannel]? ボイスチャンネルを返す
     */
    private fun getVCMentionTarget(guild: Guild): VoiceChannel?{
        return guild.getVoiceChannelById(getLong("${guild.idLong}.vc_mention", 0))
    }


    /**
     * メンション時のターゲットのVC一覧を取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [MutableList]<[VoiceChannel]> ロールの一覧を返す
     */
    fun getVCMentionTargetList(guild: Guild): MutableList<VoiceChannel>{
        //取得
        val list = getLongList("${guild.idLong}.vc_mention_list").mapNotNull { guild.getVoiceChannelById(it) }.toMutableList()

        //旧データ
        val voiceChannel = getVCMentionTarget(guild)
        if (voiceChannel != null){
            list.add(voiceChannel)
        }

        return list.distinct().toMutableList()
    }

    /**
     * メンション時のターゲット一覧にVCを追加する
     *
     * @param guild [Guild] 対象のギルド
     * @param voiceChannel [VoiceChannel] 追加するVC
     */
    fun addVCMentionTargetList(guild: Guild, voiceChannel: VoiceChannel){
        val list = getVCMentionTargetList(guild)
        list.add(voiceChannel)
        set("${guild.idLong}.vc_mention_list", list.map { it.idLong })
    }

    /**
     * メンション時のターゲット一覧からVCを削除する
     *
     * @param guild [Guild] 対象のギルド
     * @param voiceChannel [VoiceChannel] 追加するVC
     */
    fun removeVCMentionTargetList(guild: Guild, voiceChannel: VoiceChannel){
        val list = getVCMentionTargetList(guild)
        list.remove(voiceChannel)
        set("${guild.idLong}.vc_mention_list", list.map { it.idLong })
    }


    /**
     * メンション時のターゲットのロールを取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [Role]? ロールを返す
     */
    private fun getRoleMentionTarget(guild: Guild): Role?{
        return guild.getRoleById(getLong("${guild.idLong}.role_mention", 0))
    }

    /**
     * メンション時のターゲットのロール一覧を取得する
     *
     * @param guild [Guild] 対象のギルド
     * @return [MutableList]<[Role]> ロールの一覧を返す
     */
    fun getRoleMentionTargetList(guild: Guild): MutableList<Role>{
        val list = getLongList("${guild.idLong}.role_mention_list").mapNotNull { guild.getRoleById(it) }.toMutableList()

        //旧データ
        val role = getRoleMentionTarget(guild)
        if (role != null){
            list.add(role)
        }


        return list.distinct().toMutableList()
    }

    /**
     * メンション時のターゲット一覧にロールを追加する
     *
     * @param guild [Guild] 対象のギルド
     * @param role [Role] 追加するロール
     */
    fun addRoleMentionTargetList(guild: Guild, role: Role){
        val list = getRoleMentionTargetList(guild)
        list.add(role)
        set("${guild.idLong}.role_mention_list", list.map { it.idLong })
    }

    /**
     * メンション時のターゲット一覧からロールを削除する
     *
     * @param guild [Guild] 対象のギルド
     * @param role [Role] 追加するロール
     */
    fun removeRoleMentionTargetList(guild: Guild, role: Role){
        val list = getRoleMentionTargetList(guild)
        list.remove(role)
        set("${guild.idLong}.role_mention_list", list.map { it.idLong })
    }


    /**
     * コマンドの頭を取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return [String] コマンドの頭の文字列
     */
    fun getPrefix(guild: Guild): String {
        return getString("${guild.idLong}.prefix", "!")
    }

    /**
     * コマンドの頭を設定する
     *
     * @param guild [Guild] 該当のギルド
     * @param prefix [String] コマンドの頭の文字列
     */
    fun setPrefix(guild: Guild, prefix: String) {
        set("${guild.idLong}.prefix", prefix)
    }

    /**
     * ダイスモードを設定する
     *
     * @param guild [Guild] 該当のギルド
     * @param mode [DiceMode] ダイスモードを渡す
     */
    fun setDiceMode(guild: Guild, mode: DiceMode) {
        set("${guild.idLong}.diceMode", mode.toString())
    }

    /**
     * ダイスモードを取得する
     *
     * @param guild [Guild] 該当のギルド
     * @return [DiceMode] ダイスモード
     */
    fun getDiceMode(guild: Guild): DiceMode {
        val modeString = getString("${guild.idLong}.diceMode", "Default")
        return DiceMode.fromString(modeString)
    }

    /**
     * BCDiceのボットを設定する
     *
     * @param guild [Guild] 該当のギルド
     * @param botID ダイスボットのID
     */
    fun setDiceBot(guild: Guild, botID: String) {
        set("${guild.idLong}.diceBot", botID)
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
     * 一覧にタイマーを追加する
     *
     * @param guild [Guild] 対象のギルド
     * @param name [String] タイマーの名前
     * @param minutes [Int] 分
     */
    fun addTimerList(guild: Guild, name: String, minutes: Int) {
        val timers = getTimerList(guild)
        timers[name] = minutes
        setTimerList(guild, timers)
    }

    /**
     * 一覧からタイマーを削除する
     *
     * @param guild [Guild] 対象のギルド
     * @param name [String] タイマーの名前
     */
    fun removeTimerList(guild: Guild, name: String) {
        val timers = getTimerList(guild)
        timers.remove(name)
        setTimerList(guild, timers)
    }

    /**
     * タイマーの一覧を設定する
     *
     * @param guild [Guild] 対象のギルド
     * @param timers [MutableMap]<[String],[Int]> タイマーの名前と分のマップ
     */
    private fun setTimerList(guild: Guild, timers: MutableMap<String, Int>) {
        val list = timers.map { "${it.key}:${it.value}" }
        set("${guild.idLong}.list", list)
    }

    /**
     * 一覧からタイマーを実行するチャンネルを設定する
     *
     * @param guild [Guild] 対象のギルド
     * @param channel [MessageChannel] 対象のチャンネル
     */
    fun setTimerChannel(guild: Guild, channel: MessageChannel) {
        set("${guild.idLong}.channel", channel.idLong)
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
     * リストの同期設定を変更
     *
     * @param guild [Guild] 対象のギルド
     * @param bool [Boolean] true->on false->off
     */
    fun setListSync(guild: Guild, bool: Boolean){
        set("${guild.idLong}.sync", bool)
    }

    /**
     * リストの同期設定を取得
     *
     * @param guild [Guild] 対象のギルド
     * @return [Boolean] true->on false->off
     */
    fun getListSync(guild: Guild): Boolean{
        return getBoolean("${guild.idLong}.sync", false)
    }

    fun setSyncTarget(guild: Guild, targetGuild: Guild){
        set("${guild.id}.sync_target", targetGuild.idLong)
    }

    fun getSyncTarget(guild: Guild): Guild?{
        return SimpleTimer.instance.getGuild(getLong("${guild.id}.sync_target", 0))
    }

    /**
     * ファイルに保存する
     *
     */
    fun save() {
        try {
            super.save(file)
        } catch (ignore: Exception) {
        }
    }


    /**
     * メンションの設定の列挙
     *
     */
    enum class Mention {
        //メンションを行わない
        NONE,

        //@hereのメンションを行う
        HERE,

        //ロールにメンションを行う
        ROLE,

        //特定のVCにいるメンバーにメンションを行う
        TARGET_VC,

        //ボイスチャットにいるメンバーへメンションを行う
        VC;

        companion object {
            /**
             * StringからMentionへ変換
             *
             * @param string [String] 対象のString
             * @return [Mention] 変換結果。失敗時は[VC]を返す
             */
            fun fromString(string: String): Mention {
                for (mention in values()) {
                    if (mention.toString().equalsIgnoreCase(string)) {
                        return mention
                    }
                }
                return VC
            }
        }
    }

    /**
     * ダイスモードの列挙
     */
    enum class DiceMode {
        Default,
        BCDice;

        companion object {
            /**
             * StringからDiceModeへ変換
             *
             * @param string [String] 対象のString
             * @return [DiceMode] 変換結果。失敗時は[BCDice]を返す
             */
            fun fromString(string: String): DiceMode {
                for (diceMode in values()) {
                    if (diceMode.toString().equalsIgnoreCase(string)) {
                        return diceMode
                    }
                }
                return Default
            }
        }
    }

    /**
     * TTSのタイミングの列挙
     * [LV0] TTSによる通知を行わない
     * [LV1] 終了時のみ送信
     * [LV2] 終了時と、定期的な時間通知の時に実行
     * [LV3] タイマーのすべての通知（上に加えて、延長など）で実行
     */
    enum class TTSTiming(var priority: Int) {
        LV0(0),
        LV1(1),
        LV2(2),
        LV3(3);

        companion object {
            /**
             * StringからTTSTimingへ変換
             *
             * @param string [String] 対象のString
             * @return [TTSTiming] 変換結果。失敗時は[LV0]を返す
             */
            fun fromString(string: String): TTSTiming {
                //過去の情報の場合
                if (string.equalsIgnoreCase(true.toString())) {
                    return LV1
                }
                if (string.equalsIgnoreCase(false.toString())) {
                    return LV0
                }

                //通常の照合処理
                for (timing in values()) {
                    if (timing.toString().equalsIgnoreCase(string)) {
                        return timing
                    }
                }
                return LV0
            }
        }
    }
}