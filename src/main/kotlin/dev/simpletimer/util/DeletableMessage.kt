package dev.simpletimer.util

import net.dv8tion.jda.api.entities.Message

//リアクションで削除可能メッセージの一覧
val DeletableMessage = mutableMapOf<Long, Message>()