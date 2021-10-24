package net.necromagic.simpletimerKT

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.necromagic.simpletimerKT.ServerConfig.TTSTiming.*
import net.necromagic.simpletimerKT.util.equalsIgnoreCase
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