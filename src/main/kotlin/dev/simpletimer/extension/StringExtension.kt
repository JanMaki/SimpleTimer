package dev.simpletimer.extension

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