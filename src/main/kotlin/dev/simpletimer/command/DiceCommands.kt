package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.dice.DefaultDice
import dev.simpletimer.dice.Dice
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * ダイス関連のコマンド
 */
class DiceCommands {

    /**
     * ダイスを実行する
     */
    object Roll : SlashCommandManager.SlashCommand(CommandInfoPath.ROLL) {
        init {
            addOptions(createOptionData(OptionType.STRING, CommandInfoPath.DICE_OPT_DICE).setRequired(true))
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
                event.hook.sendMessage(event.guild!!.getLang().dice.longLengthWarning).queue()
                return
            }

            //コマンドを実行
            Dice().roll(event, dice)
        }
    }

    /**
     * ダイスモードを変更する
     */
    object DiceMode : SlashCommandManager.SlashCommand(CommandInfoPath.DICE_MODE) {
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
            event.hook.sendMessage(guild.getLang().dice.changeDiceMode.langFormat("**${diceMode}**")).queue()
        }
    }

    /**
     * ダイスの情報を表示する
     */
    object DiceInfo : SlashCommandManager.SlashCommand(CommandInfoPath.DICE_INFO) {
        override fun run(event: SlashCommandInteractionEvent) {
            //チャンネルを取得
            val channel = event.guildChannel

            //ギルドを取得
            val guild = event.guild ?: return

            //ダイスモードを取得
            when (guild.getGuildData().diceMode) {
                dev.simpletimer.data.enum.DiceMode.Default -> {
                    //標準ダイスのヘルプを取得して出力
                    event.hook.sendMessageEmbeds(DefaultDice.getInfoEmbed(guild.getLang())).queue()
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
    object DiceBot : SlashCommandManager.SlashCommand(CommandInfoPath.DICE_BOT) {
        init {
            addOptions(
                createOptionData(
                    OptionType.STRING, CommandInfoPath.DICE_OPT_BOT,
                    required = false,
                    autoComplete = true
                )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //オプションを取得
            val option = event.getOption("bot")

            //オプションが入力されていないかを確認
            if (option == null) {
                //メッセージを出力
                event.hook.sendMessage(event.guild!!.getLang().command.dice.selectInMenu).queue()

                CoroutineScope(Dispatchers.Default).launch {
                    //ダイスボットを変更する画面を出す
                    BCDiceManager.instance.openSelectDiceBotView(event.guildChannel)
                }
            } else {
                //IDを取得してダイスボットを変更する
                BCDiceManager.instance.selectFromInteraction(
                    event, if (option.asString.contains("／")) {
                        option.asString.split("／")[1]
                    } else {
                        option.asString
                    }
                )
            }
        }

        override fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
            //オプションを確認
            if (event.focusedOption.name != "bot") return

            //返す
            event.replyChoiceStrings(
                //入力を元に選択肢を作成 25個以下になる様にする
                BCDiceManager.instance.getGameSystems().asSequence().filter {
                    it.id.contains(
                        event.focusedOption.value, ignoreCase = true
                    ) || it.name.contains(
                        event.focusedOption.value, ignoreCase = true
                    )
                }.withIndex().filter { it.index < 25 }.map { "${it.value.name}／${it.value.id}" }.toList()
            ).queue()
        }
    }

    /**
     * 1d100
     */
    object BasicDice : SlashCommandManager.SlashCommand(CommandInfoPath.DICE_BASIC) {
        override fun run(event: SlashCommandInteractionEvent) {
            Dice().roll(event, "1d100")
        }
    }

    /**
     * シークレットダイス1d100
     */
    object BasicSecretDice :
        SlashCommandManager.SlashCommand(CommandInfoPath.DICE_SECRET) {
        override fun run(event: SlashCommandInteractionEvent) {
            Dice().roll(event, "s1d100")
        }
    }
}