package dev.simpletimer.command

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

/**
 * コマンド実装用のインターフェース
 */
interface RunCommand {
    /**
     * コマンドを実行する
     * @param user [User] 実行したユーザー
     * @param args [List] 内容
     * @param message [Message] 返信を行うクラス。
     */
    fun runCommand(user: User, args: List<String>, message: Message)
}