package dev.simpletimer.data.old_data_converter

import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.guild.GuildData
import java.io.File

/**
 * 旧ギルドのデータを変換するクラス
 */
object OldDataConverter {
    /**
     * 変換する
     *
     * @param parentDirectory 親ディレクトリ
     * @return ギルドのIDとギルドのデータをマップで返す[Map]<[Long],[GuildData]>
     */
    fun convert(parentDirectory: File): Map<Long, GuildData> {
        //結果用の変数
        val result = mutableMapOf<Long, GuildData>()

        //早期アクセス版で一時的に使っていた保存方法
        //ギルドのデータのファイル
        val guildsFile = File(parentDirectory, "guilds.yml")
        //ギルドのデータを読み込み
        try {
            //ファイルがあるかを確認
            if (!guildsFile.exists()) {
                //ファイルを作成
                guildsFile.createNewFile()
            }

            //ファイルを読み込み
            val guildsFileInputStream = guildsFile.inputStream()
            Yaml.default.decodeFromStream(GuildsData.serializer(), guildsFileInputStream).guilds.forEach {
                //結果用の変数に追加
                result[it.key] = it.value
            }
            guildsFileInputStream.close()
        } catch (ignore: EmptyYamlDocumentException) {
            //ignore
        }

        //v1の保存形式
        val configuration = ServerConfig()
        //コンフィグの中で、Long値のものを回す
        configuration.getConfigurationSection("").getKeys(false).mapNotNull { it.toLongOrNull() }.filter { it != 0L }
            .forEach { id ->
                //ギルドを取得
                val guild = SimpleTimer.instance.getGuild(id) ?: return@forEach

                //ギルドのデータを取得
                val guildData = GuildData()

                //各種データをギルドのデータに代入
                guildData.finishTTS = configuration.getTTS(guild)
                guildData.mention = configuration.getMention(guild)
                guildData.vcMentionTargets = configuration.getVCMentionTargetList(guild)
                guildData.roleMentionTargets = configuration.getRoleMentionTargetList(guild)
                guildData.diceMode = configuration.getDiceMode(guild)
                guildData.diceBot = configuration.getDiceBot(guild)

                //一覧機能
                guildData.list = linkedMapOf<String, String>().apply {
                    //すべての要素を確認
                    configuration.getTimerList(guild).forEach {
                        //頭にtimer:をつけて文字列にして代入
                        this["timer:${it.key}"] = it.value.toString()
                    }
                }

                //ターゲットチャンネルのIDからチャンネルに変換
                configuration.getTimerChannelID(guild).let { long ->
                    //チャンネルを取得
                    val channel = guild.getTextChannelById(long) ?: guild.getThreadChannelById(long)
                    //nullじゃなかったら代入
                    guildData.listTargetChannel = channel
                }

                //各種データをギルドのデータに代入
                guildData.listSync = configuration.getListSync(guild)
                guildData.syncTarget = configuration.getSyncTarget(guild)

                //結果用の変数に追加
                result[id] = guildData
            }


        //結果を返す
        return result
    }
}