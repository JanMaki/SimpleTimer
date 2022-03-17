package dev.simpletimer.util

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction

fun InteractionHook.sendMessage(message: String, ephemeral: Boolean): WebhookMessageAction<Message> {
    return this.sendMessage(message).setEphemeral(ephemeral)
}

fun InteractionHook.sendMessage(message: Message, ephemeral: Boolean): WebhookMessageAction<Message> {
    return this.sendMessage(message).setEphemeral(ephemeral)
}

fun InteractionHook.sendMessageEmbeds(
    embeds: Collection<MessageEmbed>,
    ephemeral: Boolean
): WebhookMessageAction<Message> {
    return this.sendMessageEmbeds(embeds).setEphemeral(ephemeral)
}

fun InteractionHook.sendMessageEmbeds(embed: MessageEmbed, ephemeral: Boolean): WebhookMessageAction<Message> {
    return this.sendMessageEmbeds(embed).setEphemeral(ephemeral)
}

fun InteractionHook.sendEmpty() {
    this.sendMessage("|| ||").queue {
        it.delete().queue()
    }
}