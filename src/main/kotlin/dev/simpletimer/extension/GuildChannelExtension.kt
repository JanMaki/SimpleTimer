package dev.simpletimer.extension

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.StageChannel
import net.dv8tion.jda.api.entities.VoiceChannel

/**
 * SimpleTimerのパーミッションを確認する
 *
 * @return 持っていたらtrueを返す
 */
fun GuildChannel.checkSimpleTimerPermission(): Boolean {

    //パーミッションを取得
    val permissions = this.guild.selfMember.getPermissions(this)

    //管理者の権限を確認
    val checkAdministrator = permissions.contains(Permission.ADMINISTRATOR)

    //その他のパーミッションを確認
    val otherPermission = permissions.contains(Permission.VIEW_CHANNEL) &&
            when (this) {
                is MessageChannel -> {
                    permissions.contains(Permission.MESSAGE_SEND) &&
                            permissions.contains(Permission.MESSAGE_SEND_IN_THREADS) &&
                            permissions.contains(Permission.MESSAGE_EMBED_LINKS) &&
                            permissions.contains(Permission.MESSAGE_ATTACH_FILES) &&
                            permissions.contains(Permission.MESSAGE_ADD_REACTION) &&
                            permissions.contains(Permission.MESSAGE_EXT_EMOJI) &&
                            permissions.contains(Permission.MESSAGE_MENTION_EVERYONE) &&
                            permissions.contains(Permission.MESSAGE_MANAGE) &&
                            permissions.contains(Permission.MESSAGE_HISTORY) &&
                            permissions.contains(Permission.MESSAGE_TTS)

                }
                is VoiceChannel -> {
                    permissions.contains(Permission.VOICE_CONNECT) &&
                            permissions.contains(Permission.VOICE_SPEAK)
                }
                is StageChannel -> {
                    permissions.contains(Permission.VOICE_CONNECT) &&
                            permissions.contains(Permission.VOICE_MUTE_OTHERS)
                }
                else -> {
                    true
                }
            }

    //管理者かその他のパーミッションを持っていたらtrueを返す
    return checkAdministrator || otherPermission

}