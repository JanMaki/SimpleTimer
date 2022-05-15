package dev.simpletimer.extension


/**
 * Intの拡張 0~10の時に絵文字を返す
 */
val Int.emoji: String
    get() = arrayOf("0️", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "\uD83D\uDD1F")[this]