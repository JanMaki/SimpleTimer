package dev.simpletimer.extension

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction

//InteractionHookを拡張する

/**
 * メッセージを送信
 *
 * @param message 送信するメッセージ
 * @param ephemeral 返信先にしか見えないようにする
 * @return [WebhookMessageAction]<[Message]>
 */
fun InteractionHook.sendMessage(message: String, ephemeral: Boolean): WebhookMessageAction<Message> {
    return this.sendMessage(message).setEphemeral(ephemeral)
}

/**
 * メッセージを送信
 *
 * @param message 送信するメッセージ
 * @param ephemeral 返信先にしか見えないようにする
 * @return [WebhookMessageAction]<[Message]>
 */
fun InteractionHook.sendMessage(message: Message, ephemeral: Boolean): WebhookMessageAction<Message> {
    return this.sendMessage(message).setEphemeral(ephemeral)
}

/**
 * Embedメッセージを送信
 *
 * @param embeds 送信するEmbedメッセージ
 * @param ephemeral 返信先にしか見えないようにする
 * @return [WebhookMessageAction]<[Message]>
 */
fun InteractionHook.sendMessageEmbeds(
    embeds: Collection<MessageEmbed>,
    ephemeral: Boolean
): WebhookMessageAction<Message> {
    return this.sendMessageEmbeds(embeds).setEphemeral(ephemeral)
}

/**
 * Embedメッセージを送信
 *
 * @param embed 送信するEmbedメッセージ
 * @param ephemeral 返信先にしか見えないようにする
 * @return [WebhookMessageAction]<[Message]>
 */
fun InteractionHook.sendMessageEmbeds(embed: MessageEmbed, ephemeral: Boolean): WebhookMessageAction<Message> {
    return this.sendMessageEmbeds(embed).setEphemeral(ephemeral)
}

/**
 * 空白を送信する
 *
 */
fun InteractionHook.sendEmpty() {
    this.sendMessage("|| ||").queue {
        it.delete().queue()
    }
}