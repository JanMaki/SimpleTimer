package dev.simpletimer.data

import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import dev.simpletimer.data.audio.AudioInformationData
import dev.simpletimer.data.config.ConfigData
import dev.simpletimer.data.guild.GuildData
import dev.simpletimer.extension.equalsIgnoreCase
import net.dv8tion.jda.api.entities.Guild
import java.io.File
import java.nio.file.Paths

/**
 * データ置き場
 *
 */
class DataContainer {
    //全ギルドのデータ
    private val guildDatum = mutableMapOf<Long, GuildData>()

    //コンフィグ
    var config: ConfigData

    val audioDatum = mutableListOf<AudioInformationData>()

    //jarがあるディレクトリ
    private val parentDirectory: File =
        File(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()).toString()).parentFile

    //コンフィグを保管するファイル
    private val configFile = File(parentDirectory, "config.yml")

    //ギルドのデータを保管するディレクトリ
    private val guildDirectory = File(parentDirectory, "Guild")


    //音源データを保管するディレクトリ
    private val audioDirectory = File(parentDirectory, "Audio")

    init {
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

        //ギルドを保管するディレクトリがあるかを確認
        if (!guildDirectory.exists()) guildDirectory.mkdirs()

        //ymlを読み取り
        guildDirectory.listFiles()?.filterNotNull()?.filter { it.extension == "yml" }?.forEach { file ->
            try {
                //ファイル読み込み
                val guildFileInputStream = file.inputStream()
                guildDatum[file.nameWithoutExtension.toLong()] =
                    Yaml.default.decodeFromStream(GuildData.serializer(), guildFileInputStream)
                guildFileInputStream.close()
            } catch (ignore: EmptyYamlDocumentException) {
                //空データを代入
                guildDatum[file.nameWithoutExtension.toLong()] = GuildData()
            }
        }

        //音源を保管するディレクトリがあるかを確認
        if (!audioDirectory.exists()) audioDirectory.mkdirs()

        //ymlリを読み込み
        audioDirectory.listFiles()?.filterNotNull()?.filter { it.extension == "yml" }?.forEach { file ->
            //ファイル読み込み
            val audioDataFileInputStream = file.inputStream()
            val audioData = Yaml.default.decodeFromStream(AudioInformationData.serializer(), audioDataFileInputStream)
            //ファイルの位置をフルパスに変更
            audioData.file = File(audioDirectory, audioData.file).path.toString()
            //追加
            audioDatum.add(audioData)
        }
    }

    /**
     * ギルドのデータを取得する
     *
     * @param guild 対象の[Guild]
     * @return [GuildData]
     */
    fun getGuildData(guild: Guild): GuildData {
        return guildDatum.getOrPut(guild.idLong) { GuildData() }
    }


    //デフォルトのGuildDataのYAMLの文字列
    private val defaultGuildDataYAML = Yaml.default.encodeToString(GuildData.serializer(), GuildData())

    /**
     * ギルドのデータを保存する
     *
     */
    fun saveGuildsData(guild: Guild) {
        //ギルドのデータを取得
        val guildData = getGuildData(guild)

        //デフォルトのGuildDataのYAMLと比較
        if (Yaml.default.encodeToString(GuildData.serializer(), guildData)
                .equalsIgnoreCase(defaultGuildDataYAML)
        ) return

        //ファイルを書き込み
        val guildsFileOutputStream = File(guildDirectory, "${guild.idLong}.yml").outputStream()
        Yaml.default.encodeToStream(GuildData.serializer(), guildData, guildsFileOutputStream)
        guildsFileOutputStream.close()
    }
}