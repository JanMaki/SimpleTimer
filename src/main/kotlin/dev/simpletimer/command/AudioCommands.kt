package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.audio.AudioInformationData
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * オーディオ系のコマンドの親
 *
 */
abstract class AudioCommands(langPath: CommandInfoPath) : SlashCommandManager.SlashCommand(langPath) {

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

    /**
     * ボイスチャンネルに接続をする
     *
     */
    object Connect : AudioCommands(CommandInfoPath.AUDIO_CONNECT) {
        override fun runAudio(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return

            //チャンネルを取得
            val channel = event.member?.voiceState?.channel
            //nullチェック
            if (channel == null) {
                //エラーメッセージを送信
                event.hook.sendMessage(guild.getLang().command.audio.pleaseConnectVoiceChannel).queue()
                return
            }

            //権限を確認
            if (!channel.checkSimpleTimerPermission() || !guild.selfMember.hasAccess(channel)) {
                //権限が不足しているメッセージを送信する
                event.hook.sendMessageEmbeds(SimpleTimer.instance.getErrorEmbed(channel)).queue()
                return
            }

            //接続
            guild.getAudioPlayer().connect(channel)

            //メッセージを送信
            event.hook.sendMessage(guild.getLang().command.audio.joinVoiceChannel).queue()
        }
    }

    /**
     * ボイスチャンネルから切断する
     */
    object DisConnect : AudioCommands(CommandInfoPath.AUDIO_DISCONNECT) {
        override fun runAudio(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return

            //切断
            guild.getAudioPlayer().disconnect()

            //メッセージを送信
            event.hook.sendMessage(guild.getLang().command.audio.leaveVoiceChannel).queue()
        }
    }

    /**
     * 設定されているオーディオを再生する
     *
     */
    object Listen : AudioCommands(CommandInfoPath.AUDIO_LISTEN) {
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
            event.hook.sendMessage(guild.getLang().command.audio.play).queue()
        }
    }

    /**
     * 再生するオーディオを変更する
     *
     */
    object Change : AudioCommands(CommandInfoPath.AUDIO_CHANGE) {
        //データのコンテナ
        private val dataContainer = SimpleTimer.instance.dataContainer

        init {
            //オプションを追加
            addOptions(
                createOptionData(
                    OptionType.STRING, CommandInfoPath.AUDIO_OPT_NAME,
                    required = true,
                    autoComplete = true
                )
            )
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
            event.hook.sendMessageEmbeds(getAudioInfoEmbed(guild.getLang(), audioData, "オーディオを${name}に変更しました"))
                .queue()
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

    /**
     * オーディオの一覧を表示する
     *
     */
    object AudioList : AudioCommands(CommandInfoPath.AUDIO_LIST) {
        override fun runAudio(event: SlashCommandInteractionEvent) {
            val langData: LangData = event.guild?.getLang() ?: return

            //埋め込みを作成して送信
            event.hook.sendMessageEmbeds(EmbedBuilder().apply {
                //タイトルを設定
                setTitle(langData.command.audio.audioList)
                //説明文を設定
                setDescription(langData.command.audio.audioListDescription)
                //オーディオの一覧を確認
                SimpleTimer.instance.dataContainer.audioDatum.forEach {
                    //フィールドを追加
                    this.addField("__**${it.id}**__", "${it.name}\n${it.downloadURL}\n${it.other}", false)
                }
            }.build()).queue()
        }
    }
}