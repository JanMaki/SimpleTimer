package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.button.CommunityLinkButton
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * ヘルプコマンド
 *
 */
object HelpCommand : SlashCommandManager.SlashCommand(CommandInfoPath.HELP) {
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
            addField(
                "**${langData.command.help.explanation}**",
                "[${langData.command.help.whatIsSimpleTimer}](https://simpletimer.fanbox.cc/posts/3088356)",
                true
            )
            addField(
                "",
                "[${langData.command.help.timerCommandsList}](https://simpletimer.fanbox.cc/posts/3128595)",
                true
            )
            addField(
                "",
                "[${langData.command.help.listExplanation}](https://simpletimer.fanbox.cc/posts/3088371)",
                true
            )
            addField(
                "**${langData.command.help.developInfo}**",
                "[${langData.command.help.sourceCode}](https://source.simpletimer.dev/)",
                false
            )
            addField(
                "",
                "[${langData.command.help.supportForDeveloper}](https://simpletimer.fanbox.cc/posts/3128883)",
                true
            )
            addField(
                "**${langData.command.help.show}**",
                "[${langData.command.help.termsOfUse}](https://docs.google.com/document/d/1ae23tZfLhLppYQCICte91qYN__ADQ9w4uq6OVaT3AjM/edit?usp=sharing)",
                false
            )
            addField(
                "",
                "[${langData.command.help.policy}](https://docs.google.com/document/d/1etnMMknasi3JU6V573sWWJcqcU8Im1_3b-cc2pqKE6U/edit?usp=sharing)",
                true
            )
        }.build()

        //Embedを送信
        event.hook.sendMessageEmbeds(embed).addActionRow(CommunityLinkButton.createButton(0, event.guild!!.getLang()))
            .queue()
    }
}