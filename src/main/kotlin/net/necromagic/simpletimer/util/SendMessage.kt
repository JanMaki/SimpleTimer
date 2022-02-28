package net.necromagic.simpletimer.util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.necromagic.simpletimer.SimpleTimer
import java.util.*

class SendMessage {
    companion object {
        //権限エラーの埋め込み
        val errorEmbed: MessageEmbed

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
        private val channelsMessageMap = TreeMap<MessageChannel, Message>()

        /**
         * メッセージをチャンネルに送信します
         * 過去にこのメソッドからメッセージを送っていた場合、過去のメッセージを削除します。
         *
         * @param channel [MessageChannel] 該当のチャンネル
         * @param string [String] 送信する文字列
         * @param user [User] 送信が失敗したときにエラーを送るユーザー
         */
        fun sendMessage(channel: MessageChannel, string: String, user: User) {
            try {
                if (channelsMessageMap.containsKey(channel)) {
                    channelsMessageMap[channel]?.delete()?.queue({}, {})
                }
            } catch (e: Exception) {
                //権限関係が原因の物は排除
                if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)){
                    return
                }
                Log.sendLog(e.stackTraceToString())
            } finally {
                try {
                    channel.sendMessage(string).queue {
                        channelsMessageMap[channel] = it
                    }
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
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessageEmbeds(errorEmbed).queue({ }, { })
                }
            } catch (e: Exception) {
                Log.sendLog(e.stackTraceToString())
            }
        }
    }
}