package dev.simpletimer.extension

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.internal.utils.PermissionUtil

/**
 * SimpleTimerのパーミッションを確認する
 *
 * @return 持っていたらtrueを返す
 */
fun GuildChannel.checkSimpleTimerPermission(): Boolean {
    //メンバーを取得
    val member = this.guild.selfMember

    //パーミッションを確認して返す
    return PermissionUtil.checkPermission(permissionContainer, member, Permission.VIEW_CHANNEL) &&
            //メッセージチャンネル
            if (this is MessageChannel) {
                PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_SEND) &&
                        PermissionUtil.checkPermission(
                            permissionContainer,
                            member,
                            Permission.MESSAGE_SEND_IN_THREADS
                        ) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_EMBED_LINKS) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_ATTACH_FILES) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_ADD_REACTION) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_EXT_EMOJI) &&
                        PermissionUtil.checkPermission(
                            permissionContainer,
                            member,
                            Permission.MESSAGE_MENTION_EVERYONE
                        ) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_MANAGE) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_HISTORY) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.MESSAGE_TTS)
            } else {
                true
            } &&
            //ボイスチャンネル
            if (this is VoiceChannel) {
                PermissionUtil.checkPermission(permissionContainer, member, Permission.VOICE_CONNECT) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.VOICE_SPEAK) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.VOICE_MOVE_OTHERS)
            } else {
                true
            } &&
            //ステージチャンネル
            if (this is StageChannel) {
                PermissionUtil.checkPermission(permissionContainer, member, Permission.VOICE_CONNECT) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.VOICE_MUTE_OTHERS) &&
                        PermissionUtil.checkPermission(permissionContainer, member, Permission.VOICE_MOVE_OTHERS)
            } else {
                true
            }
}