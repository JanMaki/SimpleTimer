package net.necromagic.simpletimerKT.util

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class MessageReply(private val any: Any) {

    /**
     * 返信を行う
     * @param message [String]文字列
     * @return [Any]queueした結果
     */
    fun reply(message: String): Any? {
        return when (any) {
            is Message -> {
                any.reply(message).queue()
            }
            is SlashCommandEvent -> {
                any.hook.sendMessage(message).queue({}, {})
            }
            else -> {
                null
            }
        }
    }

    /**
     * 返信を行う
     * @param embed [MessageEmbed]埋め込み
     * @return [Any]queueした結果
     */
    fun reply(embed: MessageEmbed): Any? {
        return when (any) {
            is Message -> {
                any.reply(embed).queue({}, {})
            }
            is SlashCommandEvent -> {
                any.hook.sendMessageEmbeds(embed).queue({}, {})
            }
            else -> {
                null
            }
        }
    }
}