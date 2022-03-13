package dev.simpletimer.data.audio

import kotlinx.serialization.Serializable

/**
 * 音源データの情報
 *
 * @property id 識別用のID
 * @property fileURL ファイルの直URL
 * @property name 音源の名前
 * @property right 権利表示
 * @property downloadURL URL
 * @property other その他データ
 */
@Serializable
class AudioInformationData(
    val id: String,
    var fileURL: String,
    val name: String = "",
    val right: String = "",
    val downloadURL: String = "",
    val other: String = "",
)