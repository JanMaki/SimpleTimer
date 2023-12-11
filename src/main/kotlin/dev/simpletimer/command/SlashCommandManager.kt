package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.audio.AudioCommand
import dev.simpletimer.command.dice.DiceCommands
import dev.simpletimer.command.list.ListCommand
import dev.simpletimer.command.queue.QueueCommand
import dev.simpletimer.command.timer.TimerCommands
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl

/**
 * スラッシュコマンドのマネージャー
 *
 */
object SlashCommandManager {
    //スラッシュコマンド
    val slashCommands = setOf(
        AudioCommand,
        ButtonCommand,
        ChromeCommand,
        DebugCommand,
        HelpCommand,
        LangCommand,
        QueueCommand,
        ResetCommand,
        TimerCommands.StartTimer,
        TimerCommands.Finish,
        TimerCommands.FinAll,
        TimerCommands.Add,
        TimerCommands.Stop,
        TimerCommands.Restart,
        TimerCommands.Check,
        TimerCommands.TTS,
        TimerCommands.Mention,
        DiceCommands.Roll,
        DiceCommands.Dice,
        DiceCommands.BasicDice,
        DiceCommands.BasicSecretDice,
        ListCommand.List,
    )

    /**
     * スラッシュコマンドの親
     *
     * @property deferReply 待たせる返信を行うか
     * @param langPath 言語のパス
     * @param subcommands サブコマンド
     */
    abstract class SlashCommand(
        langPath: CommandInfoPath,
        val deferReply: Boolean = true,
        vararg val subcommands: SubCommand = emptyArray()
    ) : CommandDataImpl(
        SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JPA, langPath)!!.name,
        SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JPA, langPath)!!.description
    ) {

        init {
            defaultPermissions = DefaultMemberPermissions.ENABLED

            //日本語以外を登録
            Lang.entries.filter { it != Lang.JPA }.forEach {
                //言語のデータを取得
                val langData = SimpleTimer.instance.dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                //言語のデータを設定
                setNameLocalization(it.discordLocal, langData.name)
                setDescriptionLocalization(it.discordLocal, langData.description)
            }

            //サブコマンドを登録
            if (subcommands.isNotEmpty())
                super.addSubcommands(subcommands.map { it.subCommandData })
        }

        /**
         * コマンドを実行する
         *
         * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
         */
        abstract fun run(event: SlashCommandInteractionEvent)

        /**
         * 自動補完をする
         *
         * @param event [CommandAutoCompleteInteractionEvent] 自動補完のイベント
         */
        open fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
            //デフォルトでは何もしない
        }
    }

    /**
     * サブコマンドの親
     *
     * @param langPath 言語のパス
     */
    abstract class SubCommand(langPath: CommandInfoPath, val deferReply: Boolean = true) {
        //サブコマンドを作成
        val subCommandData: SubcommandData = CommandUtil.createSubCommandData(langPath)

        /**
         * サブコマンドを実行する
         *
         * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
         */
        abstract fun run(event: SlashCommandInteractionEvent)

        /**
         * 自動補完をする
         *
         * @param event [CommandAutoCompleteInteractionEvent] 自動補完のイベント
         */
        open fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
            //デフォルトでは何もしない
        }

        fun addOptions(vararg options: OptionData) {
            subCommandData.addOptions(*options)
        }
    }
}

