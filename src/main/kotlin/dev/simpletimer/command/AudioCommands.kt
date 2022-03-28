package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.audio.AudioInformationData
import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.extension.getAudioPlayer
import dev.simpletimer.extension.getGuildData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * オーディオ系のコマンドの親
 *
 */
abstract class AudioCommands(name: String, description: String) : SlashCommandManager.SlashCommand(name, description) {

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
            event.hook.sendMessageEmbeds(getAudioInfoEmbed(audioData, "設定されているオーディオは${audioData.id}です")).queue()

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
    fun getAudioInfoEmbed(audioData: AudioInformationData, embedTitle: String): MessageEmbed {
        val embed = EmbedBuilder()

        //タイトルを設定
        embed.setTitle(embedTitle)

        //音源名
        if (audioData.name != "") {
            embed.addField("名前", audioData.name, false)
        }
        //ダウンロードリンク
        if (audioData.downloadURL != "") {
            embed.addField("リンク", audioData.downloadURL, false)
        }
        //権利表示
        if (audioData.right != "") {
            embed.addField("権利表示等", audioData.right, false)
        }
        //その他
        if (audioData.other != "") {
            embed.addField("その他", audioData.other, false)
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

    /**
     * ボイスチャンネルに接続をする
     *
     */
    object Connect : AudioCommands("audio_connect", "ボイスチャンネルに接続する") {
        override fun runAudio(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return

            //メンバーを取得
            val member = event.member
            //接続しているボイスチャンネルとステージチャンネルを取得
            val channels = guild.voiceChannels.filter {
                it.members.contains(member)
            } + guild.stageChannels.filter {
                it.members.contains(member)
            }
            //nullチェック
            if (channels.isEmpty()) {
                //エラーメッセージを送信
                event.hook.sendMessage("*ボイスチャンネルに接続してください").queue()
                return
            }

            //チャンネルを取得
            val channel = channels.first()

            //権限を確認
            if (!channel.checkSimpleTimerPermission()) {
                //権限が不足しているメッセージを送信する
                event.hook.sendMessageEmbeds(SimpleTimer.instance.getErrorEmbed(channel)).queue()
                return
            }

            //接続
            guild.getAudioPlayer().connect(channel)

            //メッセージを送信
            event.hook.sendMessage("チャンネルに参加しました").queue()
        }
    }

    /**
     * ボイスチャンネルから切断する
     */
    object DisConnect : AudioCommands("audio_disconnect", "ボイスチャンネルから抜ける") {
        override fun runAudio(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return

            //切断
            guild.getAudioPlayer().disconnect()

            //メッセージを送信
            event.hook.sendMessage("チャンネルから退出しました").queue()
        }
    }

    /**
     * 設定されているオーディオを再生する
     *
     */
    object Listen : AudioCommands("audio_listen", "設定されているオーディオを試聴する") {
        override fun runAudio(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild!!

            //オーディオを探す
            val audioDatum =
                SimpleTimer.instance.dataContainer.audioDatum.filter { it.id == guild.getGuildData().audio }
            //見つかったかを確認
            if (audioDatum.isEmpty()) {
                replyCommandError(event)
                return
            }
            //オーディオを取得
            val audioData = audioDatum.first()

            //再生
            event.guild!!.getAudioPlayer().play(audioData)

            //メッセージを送信
            event.hook.sendMessage("再生しました").queue()
        }
    }

    /**
     * 再生するオーディオを変更する
     *
     */
    object Change : AudioCommands("audio_change", "オーディオを変更する") {
        //データのコンテナ
        private val dataContainer = SimpleTimer.instance.dataContainer

        init {
            //オプションを追加
            addOption(OptionType.STRING, "名前", "オーディオの名前", true, true)
        }

        override fun runAudio(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("名前")
            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }
            //名前を取得
            val name = option.asString

            //オーディオを探す
            val audioDatum = SimpleTimer.instance.dataContainer.audioDatum.filter { it.id == name }
            //見つかったかを確認
            if (audioDatum.isEmpty()) {
                replyCommandError(event)
                return
            }
            //オーディオを取得
            val audioData = audioDatum.first()

            //ギルドを取得
            val guild = event.guild!!

            //ギルドのデータを変更
            val guildData = guild.getGuildData()

            //オーディオを設定
            guildData.audio = name
            //保存
            dataContainer.saveGuildsData(guild)

            //埋め込みを作成して送信
            event.hook.sendMessageEmbeds(getAudioInfoEmbed(audioData, "オーディオを${name}に変更しました")).queue()
        }

        override fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
            //オプション名を確認
            if (event.focusedOption.name != "名前") return

            //文字列のコレクションを返す
            event.replyChoiceStrings(dataContainer.audioDatum.map { it.id }
                .filter { it.startsWith(event.focusedOption.value) }
            ).queue()
        }
    }
}