package net.necromagic.simpletimerKT.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.necromagic.simpletimerKT.Dice
import net.necromagic.simpletimerKT.ServerConfig
import net.necromagic.simpletimerKT.SimpleTimer
import net.necromagic.simpletimerKT.util.SendMessage.Companion.sendMessage
import net.necromagic.simpletimerKT.bcdice.BCDiceManager
import net.necromagic.simpletimerKT.util.MessageReply
import net.necromagic.simpletimerKT.util.equalsIgnoreCase
import java.awt.Color

/**
 * ダイスのコマンドのクラス
 */
class DiceCommand : CommandData("dice", "ダイスの設定を変更します。"), RunCommand {

    init {
        setDefaultEnabled(true)

        addSubcommands(SubcommandData("mode", "使うダイスをDefaultかBCDiceかを切り替えます"))
        addSubcommands(SubcommandData("bot", "BCDiceで使用するダイスボットを変更します"))
        addSubcommands(SubcommandData("info", "ダイスの使い方を表示します"))
    }

    /**
     * コマンドを実行する
     * @param user [User] 実行したユーザー
     * @param channel [TextChannel] 実行したチャンネル
     * @param args [List] 内容
     * @param messageReply [MessageReply] 返信を行うクラス。
     */
    override fun runCommand(user: User, channel: TextChannel, args: List<String>, messageReply: MessageReply) {
        val prefix = SimpleTimer.instance.config.getPrefix(channel.guild)
        if (args.size >= 2) {
            when {
                args[1].equalsIgnoreCase("mode") -> {
                    //ダイスモードを反転
                    val diceMode = when (SimpleTimer.instance.config.getDiceMode(channel.guild)) {
                        ServerConfig.DiceMode.Default -> {
                            ServerConfig.DiceMode.BCDice
                        }
                        ServerConfig.DiceMode.BCDice -> {
                            ServerConfig.DiceMode.Default
                        }
                    }
                    sendMessage(channel, "ダイスモードを**$diceMode**に変更しました", user)
                    //コンフィグへ保存
                    val config = SimpleTimer.instance.config
                    config.setDiceMode(guild = channel.guild, diceMode)
                    config.save()
                    return
                }
                args[1].equalsIgnoreCase("bot") -> {
                    BCDiceManager.instance.openSelectDiceBotView(channel)
                    return
                }
                args[1].equalsIgnoreCase("info") -> {
                    when (SimpleTimer.instance.config.getDiceMode(channel.guild)) {
                        ServerConfig.DiceMode.Default -> {
                            Dice.printInfo(channel)
                        }
                        ServerConfig.DiceMode.BCDice -> {
                            BCDiceManager.instance.printInfo(channel)
                        }
                    }
                    return
                }
                else -> {
                    channel.sendMessage(createDiceHelpEmbedBuilder(prefix)).queue()
                }
            }
        } else {
            channel.sendMessage(createDiceHelpEmbedBuilder(prefix)).queue()
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