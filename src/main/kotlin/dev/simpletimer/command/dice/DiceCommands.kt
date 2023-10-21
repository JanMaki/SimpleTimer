package dev.simpletimer.command.dice

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.dice.Dice
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.getOption
import dev.simpletimer.util.CommandUtil.createOptionData
import dev.simpletimer.util.CommandUtil.replyCommandError
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
            val option = event.getOption(CommandInfoPath.DICE_OPT_DICE)

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
     * ダイス系のコマンド
     *
     */
    object Dice : SlashCommandManager.SlashCommand(
        CommandInfoPath.DICE,
        true,
        DiceMode, DiceInfo, DiceBot
    ) {
        override fun run(event: SlashCommandInteractionEvent) {
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