package dev.simpletimer.dice

import dev.simpletimer.SimpleTimer
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.util.equalsIgnoreCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

class Dice {
    fun roll(event: IReplyCallback, command: String, mentionTarget: User? = null) {
        var diceCommand = command

        //メンションを作成
        val mention = if (mentionTarget == null) "" else "<@${mentionTarget.idLong}>\n"

        //コンフィグを取得
        val config = SimpleTimer.instance.config

        //ギルドのダイスもーを確認
        when (config.getDiceMode(event.guild!!)) {
            //デフォルトのダイス
            dev.simpletimer.ServerConfig.DiceMode.Default -> {
                //シークレットダイスの確認
                val isSecret = diceCommand.substring(0, 1).equalsIgnoreCase("s")
                if (isSecret) diceCommand = diceCommand.replaceFirst("s", "")


                //ダイスの実行
                if (DefaultDice.checkDiceFormat(diceCommand)) {

                    //ダイスを作成
                    val dice = DefaultDice(diceCommand, isSecret)

                    //出力メッセージの作成
                    val sendMessage = if (isSecret) {
                        """
                            ${mention}(シークレットダイス)${dice.resultMessage}
                            """.trimIndent()
                    } else {
                        """
                            ${mention}${dice.resultMessage}
                            """.trimIndent()
                    }
                    //2000文字超えたときは失敗のログを出す
                    if (sendMessage.length >= 2000) {
                        event.hook.sendMessage(
                            """
                                ${mention}出力結果の文字数が多すぎます。
                                ダイスの内容を変更して再度実行してください。
                                """.trimIndent()
                        ).queue({}, {})
                    } else {
                        //結果を出力する
                        event.hook.sendMessage(sendMessage).queue({}, {})
                    }
                } else {
                    //構文が間違ってたらメッセージを出す
                    event.hook.sendMessage("${mention}*ダイスの構文が間違っています").queue({}, {})
                }
            }

            //BCDiceのダイス
            dev.simpletimer.ServerConfig.DiceMode.BCDice -> {
                CoroutineScope(Dispatchers.Default).launch {
                    //コマンドを実行
                    val guild = event.guild
                    if (guild != null) {
                        val roll = BCDiceManager.instance.roll(guild, diceCommand.lowercase())

                        //結果の確認
                        if (roll == null) {
                            //構文が間違ってたらメッセージを出す
                            event.hook.sendMessage("${mention}*ダイスの構文が間違っています").queue({}, {})
                        } else {
                            //結果を出力する
                            event.hook.sendMessage("${mention}${roll}").queue({}, {})
                        }
                    }
                }
            }
        }
    }
}