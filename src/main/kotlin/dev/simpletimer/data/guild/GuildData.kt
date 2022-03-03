package dev.simpletimer.data.guild

import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.enum.Mention
import dev.simpletimer.data.enum.TTSTiming
import dev.simpletimer.data.serializer.GuildSerializer
import dev.simpletimer.data.serializer.MessageChannelSerializer
import dev.simpletimer.data.serializer.RoleSerializer
import dev.simpletimer.data.serializer.VoiceChannelSerializer
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.VoiceChannel

/**
 * ギルドのデータ
 *
 * @property ttsTiming [TTSTiming]
 * @property finishTTS 終了時のTTSのメッセージ
 * @property mention [Mention]
 * @property vcMentionTargets 特定のVCへのメンション時にターゲットとなる[VoiceChannel]の[List]
 * @property roleMentionTargets 特定のRoleへのメンション時にターゲットとなる[Role]の[List]
 * @property diceMode [DiceMode]
 * @property diceBot BCDice使用時に使うダイスシステムのID
 * @property timerList タイマーリストの内容
 * @property timerChannel タイマーリストから送信するタイマーのチャンネル
 * @property listSync タイマーリストを同期するかどうか
 * @property syncTarget タイマーリストの同期元の[Guild]
 * @constructor Create empty Guild data
 */
@Serializable
data class GuildData(
    var ttsTiming: TTSTiming = TTSTiming.LV0,
    var finishTTS: String = "x番目のタイマーが終了しました",
    var mention: Mention = Mention.VC,
    var vcMentionTargets: MutableList<@Serializable(with = VoiceChannelSerializer::class) VoiceChannel?> = mutableListOf(),
    var roleMentionTargets: MutableList<@Serializable(with = RoleSerializer::class) Role?> = mutableListOf(),
    var diceMode: DiceMode = DiceMode.Default,
    var diceBot: String = "DiceBot",
    var timerList: MutableMap<String, Int> = mutableMapOf(),
    var timerChannel: @Serializable(with = MessageChannelSerializer::class) MessageChannel? = null,
    var listSync: Boolean = false,
    var syncTarget: @Serializable(with = GuildSerializer::class) Guild? = null
)
