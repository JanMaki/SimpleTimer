package net.necromagic.simpletimer.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.necromagic.simpletimer.Dice
import net.necromagic.simpletimer.ServerConfig
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.util.SendMessage.Companion.sendMessage
import net.necromagic.simpletimer.bcdice.BCDiceManager
import net.necromagic.simpletimer.util.equalsIgnoreCase
import java.awt.Color

/**
 * ダイスのコマンドのクラス
 */
class DiceCommand : CommandData("dice", "ダイスの設定を変更します。"), RunCommand {

    /**
     * コマンドを実行する
     * @param user [User] 実行したユーザー
     * @param args [List] 内容
     * @param message [Message] 返信を行うクラス。
     */
    override fun runCommand(user: User, args: List<String>, message: Message) {
        val prefix = SimpleTimer.instance.config.getPrefix(message.guild)
        val messageChannel = message.channel
        if (args.size >= 2) {
            when {
                args[1].equalsIgnoreCase("mode") -> {
                    //ダイスモードを反転
                    val diceMode = when (SimpleTimer.instance.config.getDiceMode(message.guild)) {
                        ServerConfig.DiceMode.Default -> {
                            ServerConfig.DiceMode.BCDice
                        }
                        ServerConfig.DiceMode.BCDice -> {
                            ServerConfig.DiceMode.Default
                        }
                    }
                    sendMessage(message.channel, "ダイスモードを**$diceMode**に変更しました", user)
                    //コンフィグへ保存
                    val config = SimpleTimer.instance.config
                    config.setDiceMode(guild = message.guild, diceMode)
                    config.save()
                    return
                }
                args[1].equalsIgnoreCase("bot") -> {
                    BCDiceManager.instance.openSelectDiceBotView(message.channel)
                    return
                }
                args[1].equalsIgnoreCase("info") -> {
                    when (SimpleTimer.instance.config.getDiceMode(message.guild)) {
                        ServerConfig.DiceMode.Default -> {
                            messageChannel.sendMessageEmbeds(Dice.getInfoEmbed(message.guild)).queue()
                        }
                        ServerConfig.DiceMode.BCDice -> {
                            messageChannel.sendMessageEmbeds(BCDiceManager.instance.getInfoEmbed(messageChannel, message.guild)).queue()
                        }
                    }
                    return
                }
                else -> {
                    messageChannel.sendMessageEmbeds(createDiceHelpEmbedBuilder(prefix)).queue()
                }
            }
        } else {
            messageChannel.sendMessageEmbeds(createDiceHelpEmbedBuilder(prefix)).queue()
        }
        return
    }

    /**
     * ダイスのヘルプの埋め込みメッセージを作成する
     *
     * @param prefix [String] ヘルプ内のコマンドの頭の文字列
     * @return [MessageEmbed] ヘルプの埋め込みメッセージ
     */
    private fun createDiceHelpEmbedBuilder(prefix: String): MessageEmbed {
        //新しく作成する
        val helpEmbedBuilder = EmbedBuilder()
        helpEmbedBuilder.setTitle("DiceBot")
        helpEmbedBuilder.setColor(Color.BLUE)
        helpEmbedBuilder.addField(
            "${prefix}dice mode",
            "使うダイスをDefaultかBCDiceかを切り替えます\nDefault: SimpleTimer内に実装されているダイスボット\nBCDice: BCDice( https://bcdice.org/ )を使用する。CoCなどのダイスが仕様できます",
            false
        )
        helpEmbedBuilder.addField("${prefix}dice bot", "BCDiceのダイスを設定する画面を表示します", false)
        helpEmbedBuilder.addField("${prefix}dice info", "ダイスの使い方を表示します", false)
        return helpEmbedBuilder.build()
    }
}