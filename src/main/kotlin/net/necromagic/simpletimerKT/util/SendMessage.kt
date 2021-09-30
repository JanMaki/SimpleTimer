package net.necromagic.simpletimerKT.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.necromagic.simpletimerKT.SimpleTimer
import java.util.*

class SendMessage {
    companion object {
        //権限エラーの埋め込み
        private val errorEmbed: MessageEmbed

        init {
            //権限エラーの埋め込みを作成
            val errorEmbedBuilder = EmbedBuilder()
            errorEmbedBuilder.setTitle("SimpleTimer")
            errorEmbedBuilder.setDescription(SimpleTimer.instance.version)
            errorEmbedBuilder.addField("必要な権限が付与されていません", "Botの動作に必要な権限が付与されていません", false)
            errorEmbedBuilder.addField("詳しくはこちらを参照してください", "https://bit.ly/3cvAlds", false)
            errorEmbed = errorEmbedBuilder.build()
        }


        //過去に送信したメッセージを記録
        private val channelsMessageMap = TreeMap<TextChannel, Message>()

        /**
         * メッセージをチャンネルに送信します
         * 過去にこのメソッドからメッセージを送っていた場合、過去のメッセージを削除します。
         *
         * @param channel [TextChannel] 該当のチャンネル
         * @param string [String] 送信する文字列
         * @param user [User] 送信が失敗したときにエラーを送るユーザー
         */
        fun sendMessage(channel: TextChannel, string: String, user: User) {
            try {
                if (channelsMessageMap.containsKey(channel)) {
                    channelsMessageMap[channel]?.delete()?.complete()
                }
            } catch (e: Exception) {
                Log.sendLog(e.stackTraceToString())
            } finally {
                try {
                    channelsMessageMap[channel] = channel.sendMessage(string).complete()
                } catch (e: InsufficientPermissionException) {
                    sendErrorMessageToUser(user)
                }
            }
        }


        /**
         * ユーザーにエラーメッセージを送信する
         *
         * @param user [User] 対象のユーザー
         */
        fun sendErrorMessageToUser(user: User) {
            try {
                val channel = user.openPrivateChannel().complete()
                channel.sendMessage(errorEmbed).queue({ }, { })
            } catch (e: Exception) {
                Log.sendLog(e.stackTraceToString())
            }
        }
    }
}