package net.necromagic.simpletimer.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.necromagic.simpletimer.ServerConfig
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.dice.DefaultDice
import net.necromagic.simpletimer.dice.bcdice.BCDiceManager
import net.necromagic.simpletimer.util.equalsIgnoreCase

/**
 * コマンドの管理クラス
 */
class CommandManager {
    //コマンドのリスト
    private val commands = ArrayList<CommandData>()

    init {
        //コマンドを登録
        commands.add(TimerCommand())
        commands.add(DiceCommand())
    }

    /**
     * コマンドを実行する
     * @param user [User] 実行したユーザー
     * @param args [List] 内容
     * @param message [Message] 返信を行うクラス。
     */
    fun run(user: User, args: List<String>, message: Message) {

        val command = args[0]

        val prefix = SimpleTimer.instance.config.getPrefix(message.guild)

        var result = false

        //すべてのコマンドの確認
        commands.forEach {
            if (it is RunCommand) {
                if ("${prefix}${it.name}".equalsIgnoreCase(command) || "!!${it.name}".equalsIgnoreCase(command)) {
                    it.runCommand(user, args, message)
                    result = true
                }
            }
        }

        if (!result) {
            //ダイスを確認
            if (args.isNotEmpty() && args[0].replace("||", "").length > 1 && args[0].replace("||", "").substring(0, 1)
                    .equalsIgnoreCase(prefix)
            ) {
                //扱い安い形に変更
                val builder = StringBuilder()
                args.forEach {
                    builder.append(it)
                }
                var diceCommand = builder.toString().replace(prefix, "").lowercase()

                CoroutineScope(Dispatchers.Default).launch launch@{

                    //ダイスモードの確認
                    when (SimpleTimer.instance.config.getDiceMode(message.guild)) {
                        //SimpleTimerで実装されているタイマー
                        ServerConfig.DiceMode.Default -> {
                            //シークレットダイスの確認
                            val initialCharIsS = diceCommand.substring(0, 1).equalsIgnoreCase("s")
                            val isSecret = initialCharIsS || diceCommand.contains("||")
                            if (initialCharIsS)
                                diceCommand = diceCommand.replaceFirst("s", "")
                            if (diceCommand.contains("||"))
                                diceCommand = diceCommand.replace("||", "")

                            //ダイスの実行
                            if (DefaultDice.checkDiceFormat(diceCommand)) {

                                //ダイスを作成
                                val dice = DefaultDice(diceCommand, isSecret)

                                //出力メッセージの作成
                                val sendMessage = if (isSecret) {
                                    """
                                    (シークレットダイス)${dice.resultMessage}
                                    """.trimIndent()
                                } else {
                                    """
                                    ${dice.resultMessage}
                                    """.trimIndent()
                                }
                                //2000文字超えたときは失敗のログを出す
                                if (sendMessage.length >= 2000) {
                                    message.reply(
                                        """
                                    出力結果の文字数が多すぎます。
                                    ダイスの内容を変更して再度実行してください。
                                    """.trimIndent()
                                    ).queue({}, {})
                                } else {
                                    //結果を出力する
                                    message.reply(sendMessage).queue({}, {})
                                }
                            }
                        }

                        //BCDice
                        ServerConfig.DiceMode.BCDice -> {
                            val roll =
                                BCDiceManager.instance.roll(message.guild, args[0].replace(prefix, "").lowercase())
                                    ?: return@launch
                            message.reply(roll).queue({}, {})
                        }

                    }
                }
            }
            return
        }
    }
}