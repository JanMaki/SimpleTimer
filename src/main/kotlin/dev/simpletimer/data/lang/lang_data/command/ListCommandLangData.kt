package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * 一覧コマンドの言語のデータ
 *
 * @property synced
 * @property longLengthWarning
 * @property unusableCharacter
 * @property maxEntry
 * @property add
 * @property missingEntryWarning
 * @property remove
 * @property clear
 * @property changeChannel
 * @property invalidID
 * @property startSync
 * @property targetSame
 * @property finishSync
 * @property copy
 * @property id 0->36進数
 */
@Serializable
data class ListCommandLangData(
    val synced: String = "",
    val longLengthWarning: String = "",
    val unusableCharacter: String = "",
    val maxEntry: String = "",
    val add: String = "",
    val missingEntryWarning: String = "",
    val remove: String = "",
    val clear: String = "",
    val changeChannel: String = "",
    val invalidID: String = "",
    val targetError: String = "",
    val startSync: String = "",
    val targetSame: String = "",
    val finishSync: String = "",
    val copy: String = "",
    val id: String = ""
)