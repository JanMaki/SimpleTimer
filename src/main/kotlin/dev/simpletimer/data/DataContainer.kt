package dev.simpletimer.data

import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dev.simpletimer.data.audio.AudioInformationData
import dev.simpletimer.data.config.ConfigData
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfo
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.database.data.GuildData
import dev.simpletimer.database.transaction.GuildDataTransaction
import java.io.File
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * データ置き場
 *
 */
class DataContainer {
    //コンフィグ
    var config: ConfigData

    //オーディオのデータ
    val audioDatum = mutableListOf<AudioInformationData>()

    //言語のデータ
    val langDatum = mutableMapOf<Lang, LangData>()

    //jarがあるディレクトリ
    private val parentDirectory: File =
        File(Paths.get(javaClass.protectionDomain.codeSource.location.toURI()).toString()).parentFile

    //コンフィグを保管するファイル
    private val configFile = File(parentDirectory, "config.yml")

    //音源データを保管するディレクトリ
    private val audioDirectory = File(parentDirectory, "Audio")

    //言語のデータを保管するディレクトリ
    private val langDirectory = File(parentDirectory, "Langs")

    init {
        //コンフィグの読み込み
        try {
            //ファイルがあるかを確認
            if (!configFile.exists()) {
                //ファイルを作成
                configFile.createNewFile()

                //デフォルトのデータを書き込み
                configFile.outputStream().use {
                    Yaml.default.encodeToStream(ConfigData.serializer(), ConfigData(), it)
                }
            }

            //ファイルを読み込み
            config = configFile.inputStream().use {
                Yaml.default.decodeFromStream(ConfigData.serializer(), it)
            }
        } catch (ignore: EmptyYamlDocumentException) {
            //空データを代入
            config = ConfigData()
        }


        //音源を保管するディレクトリがあるかを確認
        if (!audioDirectory.exists()) audioDirectory.mkdirs()

        //ymlリを読み込み
        audioDirectory.listFiles()?.filterNotNull()?.filter { it.extension == "yml" }?.forEach { file ->
            //ファイル読み込み
            val audioData = file.inputStream().use {
                Yaml.default.decodeFromStream(AudioInformationData.serializer(), it)
            }
            //ファイルの位置をフルパスに変更
            audioData.file = File(audioDirectory, audioData.file).path.toString()
            //追加
            audioDatum.add(audioData)
        }


        //言語を保管するディレクトリがあるかを確認
        if (!langDirectory.exists()) langDirectory.mkdirs()

        //すべての言語を確認
        for (lang in Lang.entries) {
            //ファイルを取得
            val langFile = File(langDirectory, lang.getFilePath())
            //ファイルがあるかを確認
            if (!langFile.exists()) {
                //ファイルを作成
                langFile.parentFile.mkdirs()
                langFile.createNewFile()

                //デフォルトのデータを書き込み
                langFile.outputStream().use {
                    Yaml.default.encodeToStream(LangData.serializer(), LangData(), it)
                }
            }

            //読み込んで代入
            langDatum[lang] = langFile.inputStream().use {
                Yaml.default.decodeFromStream(LangData.serializer(), it)
            }
        }
    }


    /**
     * ギルドの読み込み
     *
     */
    fun convertLegacyDataToDatabase() {
        val guildDirectory = File(parentDirectory, "Guild")

        //ギルドを保管するディレクトリがあるかを確認
        if (!guildDirectory.exists()) guildDirectory.mkdirs()

        //ymlを読み取り
        guildDirectory.listFiles()?.filterNotNull()?.filter { it.extension == "yml" }?.forEach { file ->
            try {
                val guildId = file.nameWithoutExtension.toLong()
                //ファイル読み込み
                val guildData = file.inputStream().use {
                    Yaml(configuration = YamlConfiguration()).decodeFromStream(GuildData.serializer(), it)
                }
                GuildDataTransaction.insertGuildData(guildId, guildData)
            } catch (ignore: EmptyYamlDocumentException) {
            }
        }
    }

    /**
     * 言語とパスからコマンドの情報の言語のデータを取得する
     *
     * @param path [CommandInfoPath]
     * @return [CommandInfo]
     */
    fun getCommandInfoLangData(lang: Lang, path: CommandInfoPath): CommandInfo? {
        //リフレクションの対象のクラス 最初はコマンドの言語のデータから
        var targetClass: Any = langDatum[lang]?.commandInfo ?: return null
        //KClassを取得
        var kClass: KClass<*> = targetClass::class
        //パスを.で分割する
        val propertyNames = path.langPath.split(".")
        //パスを回す
        for (name in propertyNames) {
            //プロパティーを取得
            val property = kClass.memberProperties.first { it.name == name }
            //インスタンスを取得
            targetClass = property.getter.call(targetClass) ?: break
            //インスタンスのKClassを再度取得
            kClass = targetClass::class
        }
        //キャストして返す
        return targetClass as CommandInfo
    }
}