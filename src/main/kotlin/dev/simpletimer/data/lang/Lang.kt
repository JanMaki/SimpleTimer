package dev.simpletimer.data.lang

import net.dv8tion.jda.api.interactions.DiscordLocale
import java.io.File

/**
 * 言語の一覧
 * [ISO 639](https://www.asahi-net.or.jp/~ax2s-kmtn/ref/iso639.html)
 *
 * @constructor Create empty Lang
 */
enum class Lang(val discordLocal: DiscordLocale, private val locale: String, val displayName: String) {
    JAP(DiscordLocale.JAPANESE, "", "Japanese/日本語"),
    ENG(DiscordLocale.ENGLISH_US, "en", "English/英語");

    /**
     * 言語のファイルのパスを取得する
     *
     * @return 言語のファイルの相対パス
     */
    fun getFilePath(): String {
        return "${if (locale == "") "" else "${locale}${File.separator}"}Lang.yml"
    }
}