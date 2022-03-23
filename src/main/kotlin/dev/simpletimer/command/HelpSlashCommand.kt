package dev.simpletimer.command

import dev.simpletimer.component.button.CommunityLinkButton
import dev.simpletimer.extension.sendMessageEmbeds
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * ヘルプコマンド
 *
 */
object HelpSlashCommand : SlashCommand("help", "Botの概要などを表示") {
    override fun run(event: SlashCommandInteractionEvent) {
        //ビルダー作成
        val embed = EmbedBuilder()

        //タイトル設定
        embed.setTitle("SimpleTimer")
        //画像
        embed.setThumbnail("https://i.ibb.co/YZ8PfJR/youf.png")
        //フィールド追加
        embed.addField("", "**解説**", false)
        embed.addField("", "SimpleTimerとは\nhttps://www.fanbox.cc/manage/posts/3088356", true)
        embed.addField("", "タイマーコマンド一覧\nhttps://www.fanbox.cc/manage/posts/3128595", true)
        embed.addField("", "一覧機能解説\nhttps://www.fanbox.cc/manage/posts/3088371", true)
        embed.addField("", "**開発情報**", false)
        embed.addField("", "ソースコード\nhttps://source.simpletimer.dev/", true)
        embed.addField("", "開発者へ支援\nhttps://simpletimer.fanbox.cc/posts/3128883", true)
        embed.addField("", "**表示**", false)
        embed.addField(
            "",
            "利用規約\nhttps://docs.google.com/document/d/1ae23tZfLhLppYQCICte91qYN__ADQ9w4uq6OVaT3AjM/edit?usp=sharing",
            true
        )
        embed.addField(
            "",
            "プライバシーポリシー\nhttps://docs.google.com/document/d/1etnMMknasi3JU6V573sWWJcqcU8Im1_3b-cc2pqKE6U/edit?usp=sharing",
            true
        )

        //Embedを作成し、送信
        event.hook.sendMessageEmbeds(embed.build(), true).addActionRow(CommunityLinkButton.createButton(0)).queue()
    }
}