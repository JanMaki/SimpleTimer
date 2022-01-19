package net.necromagic.simpletimer.dice

import net.dv8tion.jda.api.interactions.Interaction
import net.necromagic.simpletimer.ServerConfig
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.dice.bcdice.BCDiceManager
import net.necromagic.simpletimer.util.equalsIgnoreCase
import java.util.concurrent.Executors

class Dice {
    fun roll(event: Interaction, command: String){
        var diceCommand = command

        //コンフィグを取得
        val config = SimpleTimer.instance.config

        //ギルドのダイスもーを確認
        when (config.getDiceMode(event.guild!!)) {
            //デフォルトのダイス
            ServerConfig.DiceMode.Default -> {
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
                            (シークレットダイス)${dice.resultMessage}
                            """.trimIndent()
                    } else {
                        """
                            ${dice.resultMessage}
                            """.trimIndent()
                    }
                    //2000文字超えたときは失敗のログを出す
                    if (sendMessage.length >= 2000) {
                        event.hook.sendMessage(
                            """
                                出力結果の文字数が多すぎます。
                                ダイスの内容を変更して再度実行してください。
                                """.trimIndent()
                        ).queue()
                    } else {
                        //結果を出力する
                        event.hook.sendMessage(sendMessage).queue()
                    }
                } else {
                    //構文が間違ってたらメッセージを出す
                    event.hook.sendMessage("*ダイスの構文が間違っています").queue()
                }
            }

            //BCDiceのダイス
            ServerConfig.DiceMode.BCDice -> {
                Executors.newSingleThreadExecutor().submit {
                    //コマンドを実行
                    val guild = event.guild
                    if (guild != null) {
                        val roll = BCDiceManager.instance.roll(guild, diceCommand.lowercase())

                        //結果の確認
                        if (roll == null) {
                            //構文が間違ってたらメッセージを出す
                            event.hook.sendMessage("*ダイスの構文が間違っています").queue()
                        } else {
                            //結果を出力する
                            event.hook.sendMessage(roll).queue()
                        }
                    }
                }
            }
        }
    }
}