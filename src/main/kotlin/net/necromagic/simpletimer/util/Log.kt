package net.necromagic.simpletimer.util

import net.dv8tion.jda.api.entities.TextChannel

object Log {
    val logChannels = mutableListOf<TextChannel>()

    fun sendLog(log: String) {
        logChannels.forEach { channel ->
            try {
                channel.sendMessage(log).queue({}, {})
            } catch (ignore: Exception) {
                ignore.printStackTrace()
            }
        }
        println(log)
    }
}