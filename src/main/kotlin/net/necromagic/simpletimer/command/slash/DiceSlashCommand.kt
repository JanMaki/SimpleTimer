package net.necromagic.simpletimer.command.slash

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.necromagic.simpletimer.Dice
import net.necromagic.simpletimer.ServerConfig
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.bcdice.BCDiceManager
import net.necromagic.simpletimer.util.equalsIgnoreCase
import java.util.concurrent.Executors

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

    /**
     * ダイスモードを変更する
     */
    object DiceMode : SlashCommand("dicemode", "使うダイスをDefaultかBCDiceかを切り替える") {
        init {
            setDefaultEnabled(true)
        }

        override fun run(command: String, event: SlashCommandEvent) {

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
            //チャンネルを取得
            val channel = event.channel

            //ギルドを取得
            val guild = event.guild ?: return

            //ダイスモードを取得
            when (SimpleTimer.instance.config.getDiceMode(guild)) {
                ServerConfig.DiceMode.Default -> {
                    //標準ダイスのヘルプを取得して出力
                    event.hook.sendMessageEmbeds(Dice.getInfoEmbed(guild)).queue()
                }
                ServerConfig.DiceMode.BCDice -> {
                    //BCDiceのヘルプを取得して出力
                    Executors.newSingleThreadExecutor().submit {
                        event.hook.sendMessageEmbeds(BCDiceManager.instance.getInfoEmbed(channel, guild)).queue()
                    }
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
            event.hook.sendMessage("メニューよりボットを選択してください").complete()

            Executors.newSingleThreadExecutor().submit {
                //ダイスボットを変更する画面を出す
                BCDiceManager.instance.openSelectDiceBotView(event.channel)
            }
        }
    }

    /**
     * 1d100
     */
    object BasicDice : SlashCommand("1d100", "100面ダイスを振ります。その他の個数・面数のダイスは'/roll xDy'で使用できます") {
        override fun run(command: String, event: SlashCommandEvent) {
            val config = SimpleTimer.instance.config

            //ギルドのダイスもーを確認
            when (config.getDiceMode(event.guild!!)) {
                //デフォルトのダイス
                ServerConfig.DiceMode.Default -> {
                    val dice = Dice("1d100", false)
                    //出力メッセージの作成
                    val sendMessage =
                        """
                        ${dice.resultMessage}
                        """.trimIndent()
                    event.hook.sendMessage(sendMessage).queue()
                }


                //BCDiceのダイス
                ServerConfig.DiceMode.BCDice -> {
                    Executors.newSingleThreadExecutor().submit {
                        //コマンドを実行
                        val roll = BCDiceManager.instance.roll(event.guild!!, "1d100")

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

    /**
     * シークレットダイス1d100
     */
    object BasicSecretDice : SlashCommand("s1d100", "結果が隠された100面ダイスを振ります。その他の個数・面数のダイスは'/roll xDy'で使用できます") {
        override fun run(command: String, event: SlashCommandEvent) {
            val config = SimpleTimer.instance.config

            //ギルドのダイスもーを確認
            when (config.getDiceMode(event.guild!!)) {
                //デフォルトのダイス
                ServerConfig.DiceMode.Default -> {
                    val dice = Dice("1d100", true)
                    //出力メッセージの作成
                    val sendMessage =
                        """
                        (シークレットダイス)${dice.resultMessage}
                        """.trimIndent()
                    event.hook.sendMessage(sendMessage).queue()
                }


                //BCDiceのダイス
                ServerConfig.DiceMode.BCDice -> {
                    Executors.newSingleThreadExecutor().submit {
                        //コマンドを実行
                        val roll = BCDiceManager.instance.roll(event.guild!!, "s1d100")

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