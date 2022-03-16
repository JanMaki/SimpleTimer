package dev.simpletimer.command

object SlashCommandManager {
    val slashCommands = mutableSetOf<SlashCommand>()

    init {
        //コマンド一覧
        slashCommands.addAll(
            arrayOf(
                AudioCommand.Connect,
                AudioCommand.DisConnect,
                AudioCommand.Listen,
                AudioCommand.Change,
                ButtonSlashCommand,
                DebugCommand,
                HelpSlashCommand,
                QueueCommand.Queue,
                QueueCommand.Show,
                QueueCommand.Remove,
                QueueCommand.Clear,
                TimerSlashCommand.StartTimer,
                TimerSlashCommand.Finish,
                TimerSlashCommand.FinAll,
                TimerSlashCommand.Add,
                TimerSlashCommand.Stop,
                TimerSlashCommand.Restart,
                TimerSlashCommand.Check,
                TimerSlashCommand.TTSTiming,
                TimerSlashCommand.FinishTTS,
                TimerSlashCommand.MentionTiming,
                TimerSlashCommand.Mention,
                TimerSlashCommand.ShowRoleMentionTarget,
                TimerSlashCommand.AddRoleMentionTarget,
                TimerSlashCommand.RemoveRoleMentionTarget,
                TimerSlashCommand.ShowVCMentionTarget,
                TimerSlashCommand.AddVCMentionTarget,
                TimerSlashCommand.RemoveVCMentionTarget,
                DiceSlashCommand.Roll,
                DiceSlashCommand.DiceMode,
                DiceSlashCommand.DiceInfo,
                DiceSlashCommand.DiceBot,
                DiceSlashCommand.BasicDice,
                DiceSlashCommand.BasicSecretDice,
                TimerListSlashCommand.List,
                TimerListSlashCommand.ListAdd,
                TimerListSlashCommand.ListRemove,
                TimerListSlashCommand.ListTargetChannel,
                TimerListSlashCommand.SyncList,
                TimerListSlashCommand.GetID
            )
        )
    }
}

