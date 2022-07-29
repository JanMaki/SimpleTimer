package dev.simpletimer.data.lang.lang_data.command_info

import kotlinx.serialization.Serializable

@Serializable
data class CommandInfoLangData(
    val audioConnect: CommandInfo = CommandInfo(),
    val audioDisconnect: CommandInfo = CommandInfo(),
    val audioListen: CommandInfo = CommandInfo(),
    val audioChange: CommandInfo = CommandInfo(),
    val audioList: CommandInfo = CommandInfo(),
    val audioOPTName: CommandInfo = CommandInfo(),
    val button: CommandInfo = CommandInfo(),
    val buttonSCTimer: CommandInfo = CommandInfo(),
    val buttonSCDice: CommandInfo = CommandInfo(),
    val buttonOPTDice: CommandInfo = CommandInfo(),
    val extension: CommandInfo = CommandInfo(),
    val debug: CommandInfo = CommandInfo(),
    val roll: CommandInfo = CommandInfo(),
    val diceMode: CommandInfo = CommandInfo(),
    val diceInfo: CommandInfo = CommandInfo(),
    val diceBot: CommandInfo = CommandInfo(),
    val diceBasic: CommandInfo = CommandInfo(),
    val diceSecret: CommandInfo = CommandInfo(),
    val diceOPTDice: CommandInfo = CommandInfo(),
    val diceOPTBot: CommandInfo = CommandInfo(),
    val help: CommandInfo = CommandInfo(),
    val lang: CommandInfo = CommandInfo(),
    val list: CommandInfo = CommandInfo(),
    val listAdd: CommandInfo = CommandInfo(),
    val listRemove: CommandInfo = CommandInfo(),
    val listClear: CommandInfo = CommandInfo(),
    val listTargetChannel: CommandInfo = CommandInfo(),
    val listSync: CommandInfo = CommandInfo(),
    val listCopy: CommandInfo = CommandInfo(),
    val listID: CommandInfo = CommandInfo(),
    val listOPTTimerName: CommandInfo = CommandInfo(),
    val listOPTMinutes: CommandInfo = CommandInfo(),
    val listOPTDiceName: CommandInfo = CommandInfo(),
    val listOPTDice: CommandInfo = CommandInfo(),
    val listOPTElementName: CommandInfo = CommandInfo(),
    val listSBTimer: CommandInfo = CommandInfo()
)