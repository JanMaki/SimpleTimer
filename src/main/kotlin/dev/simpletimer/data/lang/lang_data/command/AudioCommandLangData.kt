package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * オーディオコマンドの言語のデータ
 *
 * @property settingAudio 0 -> text
 * @property name
 * @property link
 * @property copyright
 * @property other
 * @property pleaseConnectVoiceChannel
 * @property joinVoiceChannel
 * @property leaveVoiceChannel
 * @property play
 * @property audioList
 * @property audioListDescription
 */
@Serializable
data class AudioCommandLangData(
    val settingAudio: String = "",
    val name: String = "",
    val link: String = "",
    val copyright: String = "",
    val other: String = "",
    val pleaseConnectVoiceChannel: String = "",
    val joinVoiceChannel: String = "",
    val leaveVoiceChannel: String = "",
    val play: String = "",
    val audioList: String = "",
    val audioListDescription: String = ""
)
