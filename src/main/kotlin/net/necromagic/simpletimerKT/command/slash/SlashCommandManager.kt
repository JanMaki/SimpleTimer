package net.necromagic.simpletimerKT.command.slash

object SlashCommandManager {
    val slashCommands = mutableSetOf<SlashCommand>()

    init {
        //コマンド一覧
        slashCommands.addAll(arrayOf(
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
            DiceSlashCommand.Roll,
            DiceSlashCommand.DiceMode,
            DiceSlashCommand.DiceInfo,
            DiceSlashCommand.DiceBot,
            DiceSlashCommand.BasicDice,
            DiceSlashCommand.BasicSecretDice,
            TimerListSlashCommand.List,
            TimerListSlashCommand.ListAdd,
            TimerListSlashCommand.ListRemove,
            TimerListSlashCommand.TimerChannel
        ))
    }
}

