package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
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
        AudioCommands.Connect,
        AudioCommands.DisConnect,
        AudioCommands.Listen,
        AudioCommands.Change,
        AudioCommands.AudioList,
        ButtonCommand,
        ChromeCommand,
        DebugCommand,
        HelpCommand,
        LangCommand,
        QueueCommands.Queue,
        QueueCommands.Show,
        QueueCommands.Remove,
        QueueCommands.Clear,
        ResetCommand,
        TimerCommands.StartTimer,
        TimerCommands.Finish,
        TimerCommands.FinAll,
        TimerCommands.Add,
        TimerCommands.Stop,
        TimerCommands.Restart,
        TimerCommands.Check,
        TimerCommands.TTSTiming,
        TimerCommands.FinishTTS,
        TimerCommands.MentionTiming,
        TimerCommands.Mention,
        TimerCommands.ShowRoleMentionTarget,
        TimerCommands.AddRoleMentionTarget,
        TimerCommands.RemoveRoleMentionTarget,
        TimerCommands.ShowVCMentionTarget,
        TimerCommands.AddVCMentionTarget,
        TimerCommands.RemoveVCMentionTarget,
        DiceCommands.Roll,
        DiceCommands.DiceMode,
        DiceCommands.DiceInfo,
        DiceCommands.DiceBot,
        DiceCommands.BasicDice,
        DiceCommands.BasicSecretDice,
        ListCommands.List,
        ListCommands.ListAdd,
        ListCommands.ListRemove,
        ListCommands.ListClear,
        ListCommands.ListTargetChannel,
        ListCommands.SyncList,
        ListCommands.CopyList,
        ListCommands.GetID
    )

    /**
     * スラッシュコマンドの親
     *
     * @param name コマンド名
     * @param description コマンドの説明
     */
    abstract class SlashCommand(langPath: CommandInfoPath, val deferReply: Boolean = true) : CommandDataImpl(
        SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JAP, langPath)!!.name,
        SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JAP, langPath)!!.description
    ) {

        init {
            defaultPermissions = DefaultMemberPermissions.ENABLED

            //日本語以外を登録
            Lang.values().filter { it != Lang.JAP }.forEach {
                //言語のデータを取得
                val langData = SimpleTimer.instance.dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                //言語のデータを設定
                setNameLocalization(it.discordLocal, langData.name)
                setDescriptionLocalization(it.discordLocal, langData.description)
            }
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

        companion object {
            /**
             * コマンドエラーを送信する
             *
             * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
             */
            fun replyCommandError(event: SlashCommandInteractionEvent) {
                val error = event.guild?.getLang()?.command?.error ?: return
                event.hook.sendMessage(error).queue({}) {
                    event.reply(error).queue()
                }
            }

            /**
             * 言語のパスから[OptionData]を作成する
             *
             * @param type [OptionType] オプションのタイプ
             * @param langPath [CommandInfoPath] コマンドの言語情報までのパス
             * @return 作成した[OptionData]
             */
            fun createOptionData(
                type: OptionType,
                langPath: CommandInfoPath,
                required: Boolean = false,
                autoComplete: Boolean = false
            ): OptionData {
                //データコンテナを取得
                val dataContainer = SimpleTimer.instance.dataContainer

                //日本語の言語のデータを取得
                val japaneseLangData =
                    dataContainer.getCommandInfoLangData(Lang.JAP, langPath) ?: throw IllegalArgumentException()

                //オプションを作成して返す
                return OptionData(type, japaneseLangData.name, japaneseLangData.description).apply {
                    //日本語以外の言語を回す
                    Lang.values().filter { it != Lang.JAP }.forEach {
                        //言語のデータを取得
                        val langData = dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                        //ローカライズを設定
                        setNameLocalization(it.discordLocal, langData.name)
                        setDescriptionLocalization(it.discordLocal, langData.description)
                    }
                }
            }

            /**
             * 言語のパスから[SubcommandData]を作成する
             *
             * @param langPath [CommandInfoPath] コマンドの言語情報までのパス
             * @return 作成した[SubcommandData]
             */
            fun createSubCommandData(langPath: CommandInfoPath): SubcommandData {
                //データコンテナを取得
                val dataContainer = SimpleTimer.instance.dataContainer

                //日本語の言語のデータを取得
                val japaneseLangData =
                    dataContainer.getCommandInfoLangData(Lang.JAP, langPath) ?: throw IllegalArgumentException()

                //サブコマンドを作成して返す
                return SubcommandData(japaneseLangData.name, japaneseLangData.description).apply {
                    //日本語以外の言語を回す
                    Lang.values().filter { it != Lang.JAP }.forEach {
                        //言語のデータを取得
                        val langData = dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                        //ローカライズを設定
                        setNameLocalization(it.discordLocal, langData.name)
                        setDescriptionLocalization(it.discordLocal, langData.description)
                    }
                }
            }
        }
    }
}

