package dev.simpletimer.util

import net.dv8tion.jda.api.entities.TextChannel

/**
 * ログのクラス
 */
object Log {
    //ログを送信するチャンネル
    val logChannels = mutableListOf<TextChannel>()

    /**
     * ログを送信する
     */
    fun sendLog(log: String) {
        //すべてのチャンネルを確認
        logChannels.forEach { channel ->
            try {
                //メッセージを送信
                channel.sendMessage(log).queue()
            } catch (ignore: Exception) {
                ignore.printStackTrace()
            }
        }
        //コンソールにも出す
        println(log)
    }
}