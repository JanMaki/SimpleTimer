package net.necromagic.simpletimerKT.command.slash

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.necromagic.simpletimerKT.Dice
import net.necromagic.simpletimerKT.ServerConfig
import net.necromagic.simpletimerKT.SimpleTimer
import net.necromagic.simpletimerKT.bcdice.BCDiceManager
import net.necromagic.simpletimerKT.util.equalsIgnoreCase

/**
 * ダイス関連のコマンド
 */
class DiceSlashCommand {

    /**
     * ダイスを実行する
     */
    object Roll : SlashCommand("roll", "ダイスを振ります") {
        init {
            setDefaultEnabled(true)
            addOptions(OptionData(OptionType.STRING, "ダイス", "ダイスの内容 例:1d100").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("ダイス")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //取り合えず待たせる
            event.deferReply().queue()

            //コマンドを取得
            var diceCommand = option.asString

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
                    if (Dice.checkDiceFormat(diceCommand)) {

                        //ダイスを作成
                        val dice = Dice(diceCommand, isSecret)

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
                    //コマンドを実行
                    val roll = BCDiceManager.instance.roll(event.textChannel, diceCommand.lowercase())

                    //結果の確認
                    if (roll == null) {
                        //構文が間違ってたらメッセージを出す
                        event.hook.sendMessage("*ダイスの構文が間違っています").queue()
                        return
                    }

                    //結果を出力する
                    event.hook.sendMessage(roll).queue()
                }
            }
        }
    }

    /**
     * ダイスモードを変更する
     */
    object DiceMode : SlashCommand("dicemode", "使うダイスをDefaultかBCDiceかを切り替える") {
        init {
            setDefaultEnabled(true)
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //取り合えず待たせる
            event.deferReply().queue()

            //コンフィグを取得
            val config = SimpleTimer.instance.config

            //ダイスモードを反転
            val diceMode = when (config.getDiceMode(event.guild!!)) {
                ServerConfig.DiceMode.Default -> {
                    ServerConfig.DiceMode.BCDice
                }
                ServerConfig.DiceMode.BCDice -> {
                    ServerConfig.DiceMode.Default
                }
            }

            //コンフィグへ保存
            config.setDiceMode(guild = event.guild!!, diceMode)
            config.save()

            //メッセージを出力
            event.hook.sendMessage("ダイスモードを**$diceMode**に変更しました").queue()
        }
    }

    /**
     * ダイスの情報を表示する
     */
    object DiceInfo : SlashCommand("diceinfo", "ダイスの使い方を表示する") {
        override fun run(command: String, event: SlashCommandEvent) {
            //取り合えず待たせる
            event.deferReply().queue()

            //チャンネルを取得
            val channel = event.textChannel

            //ダイスモードを取得
            when (SimpleTimer.instance.config.getDiceMode(event.guild!!)) {
                ServerConfig.DiceMode.Default -> {
                    //標準ダイスのヘルプを取得して出力
                    event.hook.sendMessageEmbeds(Dice.getInfoEmbed(channel)).queue()
                }
                ServerConfig.DiceMode.BCDice -> {
                    //BCDiceのヘルプを取得して出力
                    event.hook.sendMessageEmbeds(BCDiceManager.instance.getInfoEmbed(channel)).queue()
                }
            }
        }
    }

    /**
     * ダイスボットを変更する画面を出す
     */
    object DiceBot : SlashCommand("dicebot", "BCDiceで使用するボットを変更します") {
        override fun run(command: String, event: SlashCommandEvent) {
            //メッセージを出力
            event.reply("メニューよりボットを選択してください").queue()

            //ダイスボットを変更する画面を出す
            BCDiceManager.instance.openSelectDiceBotView(event.textChannel)
        }
    }
}