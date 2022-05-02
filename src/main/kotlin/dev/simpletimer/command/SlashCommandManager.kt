package dev.simpletimer.command

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
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
    ).onEach { it.isDefaultEnabled = true }


    /**
     * スラッシュコマンドの親
     *
     * @param name コマンド名
     * @param description コマンドの説明
     */
    abstract class SlashCommand(name: String, description: String, val deferReply: Boolean = true) :
        CommandDataImpl(name, description) {

        init {
            isDefaultEnabled = true
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
                event.hook.sendMessage("*コマンドエラー").queue({}) {
                    event.reply("*コマンドエラー").queue()
                }
            }
        }
    }
}

