package dev.simpletimer.data.lang.lang_data.command_info

import kotlinx.serialization.Serializable

/**
 * コマンドの情報
 *
 * @property minutes
 * @property one
 * @property two
 * @property three
 * @property four
 * @property audioConnect
 * @property audioDisconnect
 * @property audioListen
 * @property audioChange
 * @property audioList
 * @property audioOPTName
 * @property button
 * @property buttonSCTimer
 * @property buttonSCDice
 * @property buttonOPTDice
 * @property extension
 * @property debug
 * @property roll
 * @property diceMode
 * @property diceInfo
 * @property diceBot
 * @property diceBasic
 * @property diceSecret
 * @property diceOPTDice
 * @property diceOPTBot
 * @property help
 * @property lang
 * @property list
 * @property listAdd
 * @property listRemove
 * @property listClear
 * @property listTargetChannel
 * @property listSync
 * @property listCopy
 * @property listID
 * @property listOPTTimerName
 * @property listOPTDiceName
 * @property listOPTDice
 * @property listOPTElementName
 * @property listOPTID
 * @property listSBTimer
 * @property listSBDice
 * @property listSBSyncEnable
 * @property listSBSyncDisable
 * @property queue
 * @property queueShow
 * @property queueRemove
 * @property queueClear
 * @property queueOPTAddTimer
 * @property queueOPTCheckTimer
 * @property queueOPTRemoveTimer
 * @property queueOPTNumber
 * @property queueOPTClearTimer
 * @property reset
 * @property timer
 * @property finish
 * @property finishAll
 * @property add
 * @property stop
 * @property restart
 * @property check
 * @property ttsTiming
 * @property ttsFinishMessage
 * @property mentionTiming
 * @property mention
 * @property mentionRole
 * @property mentionAddRole
 * @property mentionRemoveRole
 * @property mentionVC
 * @property mentionAddVC
 * @property mentionRemoveVC
 * @property timerOPTFinishTimer
 * @property timerOPTAddTimer
 * @property timerOPTStopTimer
 * @property timerOPTRestartTimer
 * @property timerOPTCheckTimer
 * @property timerOPTMessage
 * @property timerOPTAddRole
 * @property timerOPTRemoveRole
 * @property timerOPTAddVC
 * @property timerOPTRemoveVC
 * @property timerSBZero
 * @property timerSBOne
 * @property timerSBTwo
 * @property timerSBThree
 * @property timerMentionHere
 * @property timerMentionVC
 * @property timerMentionRole
 * @property timerMentionTargetVC
 * @property timerMentionOff
 */
@Serializable
data class CommandInfoLangData(
    val minutes: CommandInfo = CommandInfo(),
    val one: CommandInfo = CommandInfo(),
    val two: CommandInfo = CommandInfo(),
    val three: CommandInfo = CommandInfo(),
    val four: CommandInfo = CommandInfo(),
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
    val listOPTDiceName: CommandInfo = CommandInfo(),
    val listOPTDice: CommandInfo = CommandInfo(),
    val listOPTElementName: CommandInfo = CommandInfo(),
    val listOPTID: CommandInfo = CommandInfo(),
    val listOPTChannel: CommandInfo = CommandInfo(),
    val listSBTimer: CommandInfo = CommandInfo(),
    val listSBDice: CommandInfo = CommandInfo(),
    val listSBSyncEnable: CommandInfo = CommandInfo(),
    val listSBSyncDisable: CommandInfo = CommandInfo(),
    val queue: CommandInfo = CommandInfo(),
    val queueShow: CommandInfo = CommandInfo(),
    val queueRemove: CommandInfo = CommandInfo(),
    val queueClear: CommandInfo = CommandInfo(),
    val queueOPTAddTimer: CommandInfo = CommandInfo(),
    val queueOPTCheckTimer: CommandInfo = CommandInfo(),
    val queueOPTRemoveTimer: CommandInfo = CommandInfo(),
    val queueOPTNumber: CommandInfo = CommandInfo(),
    val queueOPTClearTimer: CommandInfo = CommandInfo(),
    val reset: CommandInfo = CommandInfo(),
    val timer: CommandInfo = CommandInfo(),
    val finish: CommandInfo = CommandInfo(),
    val finishAll: CommandInfo = CommandInfo(),
    val add: CommandInfo = CommandInfo(),
    val stop: CommandInfo = CommandInfo(),
    val restart: CommandInfo = CommandInfo(),
    val check: CommandInfo = CommandInfo(),
    val ttsTiming: CommandInfo = CommandInfo(),
    val ttsFinishMessage: CommandInfo = CommandInfo(),
    val mentionTiming: CommandInfo = CommandInfo(),
    val mention: CommandInfo = CommandInfo(),
    val mentionRole: CommandInfo = CommandInfo(),
    val mentionAddRole: CommandInfo = CommandInfo(),
    val mentionRemoveRole: CommandInfo = CommandInfo(),
    val mentionVC: CommandInfo = CommandInfo(),
    val mentionAddVC: CommandInfo = CommandInfo(),
    val mentionRemoveVC: CommandInfo = CommandInfo(),
    val timerOPTFinishTimer: CommandInfo = CommandInfo(),
    val timerOPTAddTimer: CommandInfo = CommandInfo(),
    val timerOPTStopTimer: CommandInfo = CommandInfo(),
    val timerOPTRestartTimer: CommandInfo = CommandInfo(),
    val timerOPTCheckTimer: CommandInfo = CommandInfo(),
    val timerOPTMessage: CommandInfo = CommandInfo(),
    val timerOPTAddRole: CommandInfo = CommandInfo(),
    val timerOPTRemoveRole: CommandInfo = CommandInfo(),
    val timerOPTAddVC: CommandInfo = CommandInfo(),
    val timerOPTRemoveVC: CommandInfo = CommandInfo(),
    val timerSBZero: CommandInfo = CommandInfo(),
    val timerSBOne: CommandInfo = CommandInfo(),
    val timerSBTwo: CommandInfo = CommandInfo(),
    val timerSBThree: CommandInfo = CommandInfo(),
    val timerMentionHere: CommandInfo = CommandInfo(),
    val timerMentionVC: CommandInfo = CommandInfo(),
    val timerMentionRole: CommandInfo = CommandInfo(),
    val timerMentionTargetVC: CommandInfo = CommandInfo(),
    val timerMentionOff: CommandInfo = CommandInfo()

)