package dev.simpletimer.data

import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.config.ConfigData
import dev.simpletimer.data.guild.GuildData
import dev.simpletimer.data.guild.GuildsData
import net.dv8tion.jda.api.entities.Guild
import java.io.File
import java.nio.file.Paths

/**
 * データ置き場
 *
 */
class DataContainer {
    //全ギルドのデータ
    private var guildsData: GuildsData

    //コンフィグ
    var config: ConfigData

    //jarがあるディレクトリ
    private val parentDirectory: File =
        File(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()).toString()).parentFile

    //ギルドのデータを保管するファイル
    private val guildsFile = File(parentDirectory, "guilds.yml")

    //コンフィグを保管するファイル
    private val configFile = File(parentDirectory, "config.yml")

    init {

        //ギルドのデータを読み込み
        try {
            //ファイルがあるかを確認
            if (!guildsFile.exists()) {
                //ファイルを作成
                guildsFile.createNewFile()
            }

            //ファイルを読み込み
            val guildsFileInputStream = guildsFile.inputStream()
            guildsData = Yaml.default.decodeFromStream(GuildsData.serializer(), guildsFileInputStream)
            guildsFileInputStream.close()
        } catch (ignore: EmptyYamlDocumentException) {
            //空データを代入
            guildsData = GuildsData(mutableMapOf())
        }

        //コンフィグの読み込み
        try {
            //ファイルがあるかを確認
            if (!configFile.exists()) {
                //ファイルを作成
                configFile.createNewFile()

                //デフォルトのデータを書き込み
                val configFileOutputStream = configFile.outputStream()
                Yaml.default.encodeToStream(ConfigData.serializer(), ConfigData(), configFileOutputStream)
                configFileOutputStream.close()
            }

            //ファイルを読み込み
            val configFileInputSimpleTimer = configFile.inputStream()
            config = Yaml.default.decodeFromStream(ConfigData.serializer(), configFileInputSimpleTimer)
            configFileInputSimpleTimer.close()
        } catch (ignore: EmptyYamlDocumentException) {
            //空データを代入
            config = ConfigData()
        }
    }

    /**
     * ギルドのデータを取得する
     *
     * @param guild 対象の[Guild]
     * @return [GuildData]
     */
    fun getGuildData(guild: Guild): GuildData {
        return guildsData.guilds.getOrPut(guild.idLong) { GuildData() }
    }

    /**
     * ギルドのデータを保存する
     *
     */
    fun saveGuildsData() {
        //ファイルを書き込み
        val guildsFileOutputStream = guildsFile.outputStream()
        Yaml.default.encodeToStream(GuildsData.serializer(), guildsData, guildsFileOutputStream)
        guildsFileOutputStream.close()
    }
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