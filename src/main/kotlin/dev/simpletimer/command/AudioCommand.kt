package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.getGuildData
import dev.simpletimer.util.getAudioPlayer
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * オーディオ系のコマンド
 */
class AudioCommand {
    /**
     * ボイスチャンネルに接続をする
     *
     */
    object Connect : SlashCommand("audio_connect", "ボイスチャンネルに接続する") {
        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return

            //メンバーを取得
            val member = event.member
            //接続しているボイスチャンネルを取得
            val channel = guild.voiceChannels.first { it.members.contains(member) }
            //nullチェック
            if (channel == null) {
                event.hook.sendMessage("*ボイスチャンネルに接続してください").queue({}, {})
                return
            }

            //接続
            guild.getAudioPlayer().connect(channel)

            //メッセージを送信
            event.hook.sendMessage("チャンネルから退出しました").queue({}, {})
        }
    }

    /**
     * ボイスチャンネルから切断する
     */
    object DisConnect : SlashCommand("audio_disconnect", "ボイスチャンネルから抜ける") {
        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return

            //切断
            guild.getAudioPlayer().disconnect()

            //メッセージを送信
            event.hook.sendMessage("チャンネルから退出しました").queue({}, {})
        }
    }

    /**
     * 設定されているオーディオを再生する
     *
     */
    object Listen : SlashCommand("audio_listen", "設定されているオーディオを試聴する") {
        override fun run(event: SlashCommandInteractionEvent) {
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
            event.hook.sendMessage("再生しました").queue({}, {})
        }
    }

    object Change : SlashCommand("audio_change", "オーディオを変更する") {
        private val dataContainer = SimpleTimer.instance.dataContainer

        init {
            //オプションを追加
            addOption(OptionType.STRING, "名前", "オーディオの名前", true, true)
        }

        override fun run(event: SlashCommandInteractionEvent) {
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

            //ギルドのデータを変更
            val guildData = event.guild!!.getGuildData()

            //オーディオを設定
            guildData.audio = name
            //保存
            dataContainer.saveGuildsData()

            val embed = EmbedBuilder()
            //タイトルを設定
            embed.setTitle("オーディオを${name}に変更しました")
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
                embed.addField("権利表示", audioData.right, false)
            }
            //その他
            if (audioData.other != "") {
                embed.addField("その他", audioData.other, false)
            }

            //メッセージを送信
            event.hook.sendMessageEmbeds(embed.build()).queue({}, {})
        }

        override fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
            //オプション名を確認
            if (event.focusedOption.name != "名前") return

            //文字列のコレクションを返す
            event.replyChoiceStrings(dataContainer.audioDatum.map { it.id }
                .filter { it.startsWith(event.focusedOption.value) }
            ).queue({}, {})
        }
    }
}