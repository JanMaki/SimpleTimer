package dev.simpletimer.data.guild

import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.enum.Mention
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.serializer.AudioChannelSerializer
import dev.simpletimer.data.serializer.GuildMessageChannelSerializer
import dev.simpletimer.data.serializer.GuildSerializer
import dev.simpletimer.data.serializer.RoleSerializer
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

/**
 * ギルドのデータ
 *
 * @property ttsTiming [NoticeTiming]
 * @property finishTTS 終了時のTTSのメッセージ
 * @property mention [Mention]
 * @property vcMentionTargets 特定のVCへのメンション時にターゲットとなる[AudioChannel]の[List]
 * @property roleMentionTargets 特定のRoleへのメンション時にターゲットとなる[Role]の[List]
 * @property diceMode [DiceMode]
 * @property diceBot BCDice使用時に使うダイスシステムのID
 * @property list タイマーリストの内容
 * @property listTargetChannel タイマーリストから送信するタイマーのチャンネル
 * @property listSync タイマーリストを同期するかどうか
 * @property syncTarget タイマーリストの同期元の[Guild]
 * @property audio 設定されているオーディオのID
 * @property needAudioAnnounce オーディオの権利表示などの
 */
@Serializable
data class GuildData(
    var ttsTiming: NoticeTiming = NoticeTiming.LV0,
    var finishTTS: String = "x番目のタイマーが終了しました",
    var mention: Mention = Mention.VC,
    var mentionTiming: NoticeTiming = NoticeTiming.LV2,
    var vcMentionTargets: MutableList<@Serializable(with = AudioChannelSerializer::class) AudioChannel?> = mutableListOf(),
    var roleMentionTargets: MutableList<@Serializable(with = RoleSerializer::class) Role?> = mutableListOf(),
    var diceMode: DiceMode = DiceMode.Default,
    var diceBot: String = "DiceBot",
    var list: LinkedHashMap<String, String> = linkedMapOf(),
    var listTargetChannel: @Serializable(with = GuildMessageChannelSerializer::class) GuildMessageChannel? = null,
    var listSync: Boolean = false,
    var syncTarget: @Serializable(with = GuildSerializer::class) Guild? = null,
    var audio: String = "Voice",
    var needAudioAnnounce: Boolean = true,
    var lang: Lang = Lang.JAP
)
