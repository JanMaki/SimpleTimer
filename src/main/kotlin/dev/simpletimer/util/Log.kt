package dev.simpletimer.util

import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory

/**
 * ログのクラス
 */
object Log {
    //ログを送信するチャンネル
    val logChannels = mutableListOf<TextChannel>()

    //ロガー
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * ログを送信する
     */
    fun sendLog(log: String, isError: Boolean = false) {
        //ログを切りとりする時に使用
        var logText = log
        //ログを切り取った結果
        val logs = mutableListOf<String>()
        //文字数を確認する
        while (logText.length > 1500) {
            //最初の1500文字をリストに追加
            logs.add(logText.substring(0, 1500))
            //最初の1500文字を削除
            logText = logText.substring(1500, logText.length)
        }
        //残っている部分があるかを確認
        if (logText.isNotEmpty() || logText.isNotBlank()) {
            //追加
            logs.add(logText)
        }

        //すべてのチャンネルを確認
        logChannels.forEach { channel ->
            //すべてのログ
            logs.forEach {
                //メッセージを送信
                channel.sendMessage(it).queue()
            }
        }

        //コンソールにも出す
        if (isError) {
            logger.error(log)
        } else {
            logger.info(log)
        }
    }
}