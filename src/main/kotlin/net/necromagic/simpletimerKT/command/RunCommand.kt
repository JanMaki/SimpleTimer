package net.necromagic.simpletimerKT.command

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.necromagic.simpletimerKT.util.MessageReply

/**
 * コマンド実装用のインターフェース
 */
interface RunCommand {
    /**
     * コマンドを実行する
     * @param user [User] 実行したユーザー
     * @param channel [TextChannel] 実行したチャンネル
     * @param args [List] 内容
     * @param messageReply [MessageReply] 返信を行うクラス。
     */
    fun runCommand(user: User, channel: TextChannel, args: List<String>, messageReply: MessageReply)
}