package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.dice.DefaultDice
import dev.simpletimer.dice.Dice
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.extension.getGuildData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * ダイス関連のコマンド
 */
class DiceCommands {

    /**
     * ダイスを実行する
     */
    object Roll : SlashCommandManager.SlashCommand("roll", "ダイスを振ります") {
        init {
            addOptions(OptionData(OptionType.STRING, "ダイス", "ダイスの内容 例:1d100").setRequired(true))
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("ダイス")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //ダイスの内容を取得
            val dice = option.asString

            //文字数制限
            if (dice.length >= 30) {
                event.hook.sendMessage("*名前の文字数は30文字以下にしてください").queue()
                return
            }

            //コマンドを実行
            Dice().roll(event, dice)
        }
    }

    /**
     * ダイスモードを変更する
     */
    object DiceMode : SlashCommandManager.SlashCommand("dice_mode", "使うダイスをDefaultかBCDiceかを切り替える") {
        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            //ダイスモードを反転
            val diceMode = when (guildData.diceMode) {
                dev.simpletimer.data.enum.DiceMode.Default -> {
                    dev.simpletimer.data.enum.DiceMode.BCDice
                }
                dev.simpletimer.data.enum.DiceMode.BCDice -> {
                    dev.simpletimer.data.enum.DiceMode.Default
                }
            }

            //ギルドのデータへ保存
            guildData.diceMode = diceMode
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを出力
            event.hook.sendMessage("ダイスモードを**$diceMode**に変更しました").queue()
        }
    }

    /**
     * ダイスの情報を表示する
     */
    object DiceInfo : SlashCommandManager.SlashCommand("dice_info", "ダイスの使い方を表示する") {
        override fun run(event: SlashCommandInteractionEvent) {
            //チャンネルを取得
            val channel = event.channel

            //ギルドを取得
            val guild = event.guild ?: return

            //ダイスモードを取得
            when (guild.getGuildData().diceMode) {
                dev.simpletimer.data.enum.DiceMode.Default -> {
                    //標準ダイスのヘルプを取得して出力
                    event.hook.sendMessageEmbeds(DefaultDice.getInfoEmbed()).queue()
                }
                dev.simpletimer.data.enum.DiceMode.BCDice -> {
                    //BCDiceのヘルプを取得して出力
                    CoroutineScope(Dispatchers.Default).launch {
                        event.hook.sendMessageEmbeds(BCDiceManager.instance.getInfoEmbed(channel, guild))
                            .queue()
                    }
                }
            }
        }
    }

    /**
     * ダイスボットを変更する画面を出す
     */
    object DiceBot : SlashCommandManager.SlashCommand("dice_bot", "BCDiceで使用するボットを変更します") {
        override fun run(event: SlashCommandInteractionEvent) {
            //メッセージを出力
            event.hook.sendMessage("メニューよりボットを選択してください").queue()

            CoroutineScope(Dispatchers.Default).launch {
                //ダイスボットを変更する画面を出す
                BCDiceManager.instance.openSelectDiceBotView(event.channel)
            }
        }
    }

    /**
     * 1d100
     */
    object BasicDice : SlashCommandManager.SlashCommand("1d100", "100面ダイスを振ります。その他の個数・面数のダイスは'/roll xDy'で使用できます") {
        override fun run(event: SlashCommandInteractionEvent) {
            Dice().roll(event, "1d100")
        }
    }

    /**
     * シークレットダイス1d100
     */
    object BasicSecretDice :
        SlashCommandManager.SlashCommand("s1d100", "結果が隠された100面ダイスを振ります。その他の個数・面数のダイスは'/roll xDy'で使用できます") {
        override fun run(event: SlashCommandInteractionEvent) {
            Dice().roll(event, "s1d100")
        }
    }
}