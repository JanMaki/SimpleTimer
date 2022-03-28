package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.button.CommunityLinkButton
import dev.simpletimer.extension.sendMessageEmbeds
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * ヘルプコマンド
 *
 */
object HelpCommand : SlashCommandManager.SlashCommand("help", "Botの概要などを表示") {
    //送信する埋め込み
    private val embed = EmbedBuilder().apply {
        //タイトル設定
        setTitle("SimpleTimer")
        //バージョンを説明文に入れる
        setDescription(SimpleTimer.instance.version)
        //画像
        setThumbnail("https://i.ibb.co/YZ8PfJR/youf.png")
        //フィールド追加
        addField("", "**解説**", false)
        addField("", "SimpleTimerとは\nhttps://simpletimer.fanbox.cc/posts/3088356", true)
        addField("", "タイマーコマンド一覧\nhttps://simpletimer.fanbox.cc/posts/3128595", true)
        addField("", "一覧機能解説\nhttps://simpletimer.fanbox.cc/posts/3088371", true)
        addField("", "**開発情報**", false)
        addField("", "ソースコード\nhttps://source.simpletimer.dev/", true)
        addField("", "開発者へ支援\nhttps://simpletimer.fanbox.cc/posts/3128883", true)
        addField("", "**表示**", false)
        addField(
            "",
            "利用規約\nhttps://docs.google.com/document/d/1ae23tZfLhLppYQCICte91qYN__ADQ9w4uq6OVaT3AjM/edit?usp=sharing",
            true
        )
        addField(
            "",
            "プライバシーポリシー\nhttps://docs.google.com/document/d/1etnMMknasi3JU6V573sWWJcqcU8Im1_3b-cc2pqKE6U/edit?usp=sharing",
            true
        )
    }.build()

    override fun run(event: SlashCommandInteractionEvent) {
        //Embedを送信
        event.hook.sendMessageEmbeds(embed, true).addActionRow(CommunityLinkButton.createButton(0)).queue()
    }
}