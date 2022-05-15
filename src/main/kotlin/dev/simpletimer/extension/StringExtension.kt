package dev.simpletimer.extension

import dev.simpletimer.data.lang.lang_data.LangData

//Stringを拡張する

/**
 * Stringの拡張
 * JavaにあったString.equalsIgnoreCaseの再現
 * 大文字小文字関係なく文字列の判定を行う
 *
 * @param other [String] 対象の文字列
 * @return [Boolean] 合致したかの結果
 */
fun String.equalsIgnoreCase(other: String): Boolean {
    return equals(other, true)
}

/**
 * 言語のフォーマットに合わせてテキストをリプレースする
 *
 * @param args 代入する値
 * @return 置き換えた結果の[String]
 */
fun String.langFormat(vararg args: Any): String {
    var result = this

    println(args)

    args.withIndex().forEach {
        result = result.replace("%${it.index}", it.value.toString())
    }

    return result
}

/**
 * 言語のフォーマットに合わせて分と秒のテキストをリプレースする
 *
 * @param langData [LangData] 言語のデータ
 * @param minutes 分
 * @param seconds　秒
 * @return　置き換えた結果の[String]
 */
fun String.langFormatTime(langData: LangData, minutes: Int, seconds: Int): String {
    return langFormat("${langData.timer.minutes.langFormat(minutes)}${langData.timer.seconds.langFormat(seconds)}")
}