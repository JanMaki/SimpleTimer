package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.button.CommunityLinkButton
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * ヘルプコマンド
 *
 */
object HelpCommand : SlashCommandManager.SlashCommand("help", "Botの概要などを表示") {
    override fun run(event: SlashCommandInteractionEvent) {
        //言語のデータ
        val langData = event.guild?.getLang() ?: return

        //送信する埋め込み
        val embed = EmbedBuilder().apply {
            //タイトル設定
            setTitle("SimpleTimer")
            //バージョンを説明文に入れる
            setDescription(SimpleTimer.instance.version)
            //画像
            setThumbnail("https://i.ibb.co/YZ8PfJR/youf.png")
            //フィールド追加
            addField("", "**${langData.command.help.explanation}**", false)
            addField("", "${langData.command.help.whatIsSimpleTimer}\nhttps://simpletimer.fanbox.cc/posts/3088356", true)
            addField("", "${langData.command.help.timerCommandsList}\nhttps://simpletimer.fanbox.cc/posts/3128595", true)
            addField("", "${langData.command.help.listExplanation}\nhttps://simpletimer.fanbox.cc/posts/3088371", true)
            addField("", "**${langData.command.help.developInfo}**", false)
            addField("", "${langData.command.help.sourceCode}\nhttps://source.simpletimer.dev/", true)
            addField("", "${langData.command.help.supportForDeveloper}\nhttps://simpletimer.fanbox.cc/posts/3128883", true)
            addField("", "**${langData.command.help.show}**", false)
            addField(
                "",
                "${langData.command.help.termsOfUse}\nhttps://docs.google.com/document/d/1ae23tZfLhLppYQCICte91qYN__ADQ9w4uq6OVaT3AjM/edit?usp=sharing",
                true
            )
            addField(
                "",
                "${langData.command.help.policy}\nhttps://docs.google.com/document/d/1etnMMknasi3JU6V573sWWJcqcU8Im1_3b-cc2pqKE6U/edit?usp=sharing",
                true
            )
        }.build()

        //Embedを送信
        event.hook.sendMessageEmbeds(embed).addActionRow(CommunityLinkButton.createButton(0, event.guild!!.getLang())).queue()
    }
}