package dev.simpletimer.command.slash

object SlashCommandManager {
    val slashCommands = mutableSetOf<SlashCommand>()

    init {
        //コマンド一覧
        slashCommands.addAll(
            arrayOf(
                ButtonSlashCommand,
                TimerSlashCommand.StartTimer,
                TimerSlashCommand.Finish,
                TimerSlashCommand.FinAll,
                TimerSlashCommand.Add,
                TimerSlashCommand.Stop,
                TimerSlashCommand.Restart,
                TimerSlashCommand.Check,
                TimerSlashCommand.TTS,
                TimerSlashCommand.FinishTTS,
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
                TimerListSlashCommand.TimerChannel,
                TimerListSlashCommand.SyncList,
                TimerListSlashCommand.GetID
            )
        )
    }
}

