package net.necromagic.simpletimer.command.slash

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.necromagic.simpletimer.ServerConfig
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.dice.DefaultDice
import net.necromagic.simpletimer.dice.Dice
import net.necromagic.simpletimer.dice.bcdice.BCDiceManager

/**
 * ダイス関連のコマンド
 */
class DiceSlashCommand {

    /**
     * ダイスを実行する
     */
    object Roll : SlashCommand("roll", "ダイスを振ります") {
        init {
            isDefaultEnabled = true
            addOptions(OptionData(OptionType.STRING, "ダイス", "ダイスの内容 例:1d100").setRequired(true))
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("ダイス")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //コマンドを実行
            Dice().roll(event, option.asString)
        }
    }

    /**
     * ダイスモードを変更する
     */
    object DiceMode : SlashCommand("dice_mode", "使うダイスをDefaultかBCDiceかを切り替える") {
        init {
            isDefaultEnabled = true
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {

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
            event.hook.sendMessage("ダイスモードを**$diceMode**に変更しました").queue({}, {})
        }
    }

    /**
     * ダイスの情報を表示する
     */
    object DiceInfo : SlashCommand("dice_info", "ダイスの使い方を表示する") {
        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //チャンネルを取得
            val channel = event.channel

            //ギルドを取得
            val guild = event.guild ?: return

            //ダイスモードを取得
            when (SimpleTimer.instance.config.getDiceMode(guild)) {
                ServerConfig.DiceMode.Default -> {
                    //標準ダイスのヘルプを取得して出力
                    event.hook.sendMessageEmbeds(DefaultDice.getInfoEmbed(guild)).queue({}, {})
                }
                ServerConfig.DiceMode.BCDice -> {
                    //BCDiceのヘルプを取得して出力
                    CoroutineScope(Dispatchers.Default).launch {
                        event.hook.sendMessageEmbeds(BCDiceManager.instance.getInfoEmbed(channel, guild)).queue({}, {})
                    }
                }
            }
        }
    }

    /**
     * ダイスボットを変更する画面を出す
     */
    object DiceBot : SlashCommand("dice_bot", "BCDiceで使用するボットを変更します") {
        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //メッセージを出力
            event.hook.sendMessage("メニューよりボットを選択してください").queue({}, {})

            CoroutineScope(Dispatchers.Default).launch {
                //ダイスボットを変更する画面を出す
                BCDiceManager.instance.openSelectDiceBotView(event.channel)
            }
        }
    }

    /**
     * 1d100
     */
    object BasicDice : SlashCommand("1d100", "100面ダイスを振ります。その他の個数・面数のダイスは'/roll xDy'で使用できます") {
        override fun run(command: String, event: SlashCommandInteractionEvent) {
            Dice().roll(event, "1d100")
        }
    }

    /**
     * シークレットダイス1d100
     */
    object BasicSecretDice : SlashCommand("s1d100", "結果が隠された100面ダイスを振ります。その他の個数・面数のダイスは'/roll xDy'で使用できます") {
        override fun run(command: String, event: SlashCommandInteractionEvent) {
            Dice().roll(event, "s1d100")
        }
    }
}