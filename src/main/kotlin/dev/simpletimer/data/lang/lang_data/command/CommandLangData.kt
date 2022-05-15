package dev.simpletimer.data.lang.lang_data.command

import kotlinx.serialization.Serializable

/**
 * コマンド関係の言語のデータ
 *
 * @property audio オーディオコマンドの言語のデータ
 * @property button ボタンコマンドの言語ののデータ
 * @property chrome Chromeの拡張機能コマンドの言語のデータ
 * @property dice ダイスコマンドの言語のデータ
 * @property help ヘルプコマンドの言語のデータ
 * @property lang 言語変更コマンドの言語のデータ
 * @property list 一覧コマンドの言語のデータ
 * @property queue キューコマンドの言語のデータ
 * @property reset リセットコマンドの言語のデータ
 */
@Serializable
data class CommandLangData(
    val audio: AudioCommandLangData = AudioCommandLangData(),
    val button: ButtonCommandLangData = ButtonCommandLangData(),
    val chrome: ChromeCommandLangData = ChromeCommandLangData(),
    val dice: DiceCommandLangData = DiceCommandLangData(),
    val help: HelpCommandLangData = HelpCommandLangData(),
    val lang: LangCommandLangData = LangCommandLangData(),
    val list: ListCommandLangData = ListCommandLangData(),
    val queue: QueueCommandLangData = QueueCommandLangData(),
    val reset: ResetCommandLangData = ResetCommandLangData()
)