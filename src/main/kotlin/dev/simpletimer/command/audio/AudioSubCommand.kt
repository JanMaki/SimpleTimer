package dev.simpletimer.command.audio

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.audio.AudioInformationData
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * オーディオ系のコマンドの親
 *
 */
abstract class AudioSubCommand(langPath: CommandInfoPath) : SlashCommandManager.SubCommand(langPath) {

    override fun run(event: SlashCommandInteractionEvent) {
        //ギルドを取得
        val guild = event.guild!!

        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //権利表示などのアナウンスが必要かを確認
        if (guildData.needAudioAnnounce) {

            //Voiceのオーディオを取得
            val audioData = SimpleTimer.instance.dataContainer.audioDatum.first { it.id == "Voice" }

            //メッセージを送信する
            event.hook.sendMessageEmbeds(
                getAudioInfoEmbed(
                    guild.getLang(),
                    audioData,
                    guild.getLang().command.audio.settingAudio.langFormat(audioData.id)
                )
            ).queue()

            //オーディオの通知をしなくして保存
            guildData.needAudioAnnounce = false
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)
        }

        //子のコマンドを実行
        runAudio(event)
    }

    /**
     * 音源の情報の埋め込みを作る
     *
     * @param audioData [AudioInformationData]
     * @param embedTitle 埋め込みのタイトル
     * @return 作成した[MessageEmbed]
     */
    fun getAudioInfoEmbed(langData: LangData, audioData: AudioInformationData, embedTitle: String): MessageEmbed {
        val embed = EmbedBuilder()

        //タイトルを設定
        embed.setTitle(embedTitle)

        //音源名
        if (audioData.name != "") {
            embed.addField(langData.command.audio.name, audioData.name, false)
        }
        //ダウンロードリンク
        if (audioData.downloadURL != "") {
            embed.addField(langData.command.audio.link, audioData.downloadURL, false)
        }
        //権利表示
        if (audioData.right != "") {
            embed.addField(langData.command.audio.copyright, audioData.right, false)
        }
        //その他
        if (audioData.other != "") {
            embed.addField(langData.command.audio.other, audioData.other, false)
        }

        //Buildして返す
        return embed.build()
    }

    /**
     * オーディオのコマンドを実行する
     *
     * @param event イベント[SlashCommandInteractionEvent]
     */
    abstract fun runAudio(event: SlashCommandInteractionEvent)
}