package dev.simpletimer.database.table

import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.enum.Mention
import dev.simpletimer.data.enum.NoticeTiming
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.serializer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

/**
 * ギルドのデータの[Table]
 *
 */
object GuildDataTable : Table("guild_data") {
    //自動採番キー
    private val guildDataId = long("guild_data_id").autoIncrement()

    //DiscordのギルドのID
    val discordGuildId = long("discord_guild_id")

    //データ本体
    val ttsTiming = enumeration<NoticeTiming>("tts_timing").default(NoticeTiming.LV0)
    val finishTTS = text("finish_tts").default("x番目のタイマーが終了しました")
    val mention = enumeration<Mention>("mention").default(Mention.VC)
    val mentionTiming = enumeration<NoticeTiming>("mention_timing").default(NoticeTiming.LV2)
    val vcMentionTargets =
        json<MutableList<@Serializable(with = AudioChannelSerializer::class) AudioChannel?>>(
            "vc_mention_targets",
            Json.Default
        ).default(mutableListOf())
    val roleMentionTargets =
        json<MutableList<@Serializable(with = RoleSerializer::class) Role?>>(
            "json_mention_targets",
            Json.Default
        ).default(
            mutableListOf()
        )
    val diceMode = enumeration<DiceMode>("dice_mode").default(DiceMode.Default)
    val diceBot = text("dice_bot").default("DiceBot")
    val list = json<LinkedHashMap<String, String>>("list", Json.Default).default(linkedMapOf())
    val listTargetChannel = json<@Serializable(with = GuildMessageChannelSerializer::class) GuildMessageChannel>(
        "list_target_channel",
        Json.Default
    ).nullable()
    val listSync = bool("list_sync").default(false)
    val syncTarget = json<@Serializable(with = GuildSerializer::class) Guild>("sync_target", Json.Default).nullable()
    val audio = text("audio").default("Voice")
    val needAudioAnnounce = bool("need_audio_announce").default(true)
    val lang = json<@Serializable(with = LangSerializer::class) Lang>("lang", Json.Default).default(Lang.JPA)


    override val primaryKey = PrimaryKey(guildDataId)
}