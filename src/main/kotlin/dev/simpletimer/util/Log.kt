package dev.simpletimer.util

import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory

/**
 * ログのクラス
 */
object Log {
    //ログを送信するチャンネル
    val logChannels = mutableListOf<TextChannel>()

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * ログを送信する
     */
    fun sendLog(log: String, isError: Boolean = false) {
        //すべてのチャンネルを確認
        logChannels.forEach { channel ->
            //メッセージを送信
            channel.sendMessage(log).queue()
        }

        //コンソールにも出す
        if (isError) {
            logger.error(log)
        } else {
            logger.info(log)
        }
    }
}