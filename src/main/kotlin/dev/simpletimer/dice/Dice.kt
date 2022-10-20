package dev.simpletimer.dice

import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.extension.equalsIgnoreCase
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
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

        //言語のデータ
        val langData = guild.getLang()

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
                    val dice = DefaultDice(guild.getLang(), diceCommand, isSecret)

                    //出力メッセージの作成
                    val sendMessage = if (isSecret) {
                        langData.dice.secret.langFormat("${mention}${langData.dice.secret}${dice.resultMessage}")
                    } else {
                        """
                        ${mention}${dice.resultMessage}
                        """.trimIndent()
                    }
                    //2000文字超えたときは失敗のログを出す
                    return if (sendMessage.length >= 2000) {
                        langData.dice.longRangeWaring.langFormat(mention)
                    } else {
                        //結果を出力する
                        sendMessage
                    }
                } else {
                    //構文が間違ってたらメッセージを出す
                    return langData.dice.wrongFormat.langFormat(mention)
                }
            }

            //BCDiceのダイス
            DiceMode.BCDice -> {
                //コマンドを実行
                val roll = BCDiceManager.instance.roll(guild, diceCommand.lowercase())

                //結果の確認
                return if (roll == null) {
                    //構文が間違ってたらメッセージを出す
                    langData.dice.wrongFormat.langFormat(mention)
                } else {
                    //結果を出力する
                    "${mention}${roll}"
                }
            }
        }
    }
}