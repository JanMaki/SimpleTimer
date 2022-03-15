package dev.simpletimer.dice

import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.util.equalsIgnoreCase
import dev.simpletimer.util.getGuildData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

/**
 * ダイスロールを行う時に使う
 *
 */
class Dice {
    /**
     * ダイスをロールする
     * チャンネルにメッセージを送信する
     *
     * @param channel 送信先の[GuildMessageChannel]
     * @param command 実行するダイスのコマンド
     * @param mentionTarget メンションの対象の[User]
     */
    fun roll(channel: GuildMessageChannel, command: String, mentionTarget: User? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            channel.sendMessage(getRollResultText(channel.guild, command, mentionTarget)).queue()
        }
    }

    /**
     * ダイスをロールする
     * スラッシュコマンドやボタンに応答するときに使う
     *
     * @param event 応答する[IReplyCallback]
     * @param command 実行するダイスのコマンド
     * @param mentionTarget メンションの対象の[User]
     */
    fun roll(event: IReplyCallback, command: String, mentionTarget: User? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            event.hook.sendMessage(getRollResultText(event.guild!!, command, mentionTarget)).queue()
        }
    }

    /**
     * コマンドを実行した結果のテキストを取得する
     *
     * @param guild コマンドを実行する[Guild]
     * @param command 実行するダイスのコマンド
     * @param mentionTarget メンションの対象の[User]
     * @return
     */
    private fun getRollResultText(guild: Guild, command: String, mentionTarget: User?): String {
        var diceCommand = command

        //メンションを作成
        val mention = if (mentionTarget == null) "" else "<@${mentionTarget.idLong}>\n"

        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //ギルドのダイスモードを確認
        when (guildData.diceMode) {
            //デフォルトのダイス
            DiceMode.Default -> {
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
                    return if (sendMessage.length >= 2000) {
                        """
                        ${mention}出力結果の文字数が多すぎます。
                        ダイスの内容を変更して再度実行してください。
                        """.trimIndent()
                    } else {
                        //結果を出力する
                        sendMessage
                    }
                } else {
                    //構文が間違ってたらメッセージを出す
                    return "${mention}*ダイスの構文が間違っています"
                }
            }

            //BCDiceのダイス
            DiceMode.BCDice -> {
                //コマンドを実行
                val roll = BCDiceManager.instance.roll(guild, diceCommand.lowercase())

                //結果の確認
                return if (roll == null) {
                    //構文が間違ってたらメッセージを出す
                    "${mention}*ダイスの構文が間違っています"
                } else {
                    //結果を出力する
                    "${mention}${roll}"
                }
            }
        }
    }
}