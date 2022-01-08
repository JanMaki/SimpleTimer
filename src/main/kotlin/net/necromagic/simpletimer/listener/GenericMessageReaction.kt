package net.necromagic.simpletimer.listener

import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimer.timer.Timer
import net.necromagic.simpletimer.bcdice.BCDiceManager

import java.util.*
import java.util.concurrent.Executors

/**
 * リアクションの操作を行うクラス
 *
 */
class GenericMessageReaction : ListenerAdapter() {
    //各チャンネルのクールタイム用のセット
    private val coolTime = TreeSet<Long>()

    /**
     * メッセージにリアクションがされた時に呼び出される
     *
     * @param event [GenericMessageReactionEvent] イベント
     */
    override fun onGenericMessageReaction(event: GenericMessageReactionEvent) {
        super.onGenericMessageReaction(event)

        //ユーザーの確認
        val user = event.user
        if (user != null && user.isBot) return

        //クールタイムの確認
        val idLong = event.messageIdLong
        if (coolTime.contains(idLong)) return

        //タイマーの確認
        val timer = Timer.getTimer(idLong)
        if (timer != null) {

            //リアクションの確認・処理
            when (event.reactionEmote.name) {
                "◀" -> timer.restart()
                "\uD83D\uDED1" -> timer.finish()
                "❌" -> timer.end()
                else -> {
                    when (event.reactionEmote.name) {
                        "⏸" -> timer.stop()
                        "1️⃣" -> timer.add(1*60)
                        "3️⃣" -> timer.add(3*60)
                        "5️⃣" -> timer.add(5*60)
                        "\uD83D\uDD1F" -> timer.add(10*60)
                        else -> return
                    }
                }
            }

        }

        //ダイスの確認
        else if (BCDiceManager.instance.isSelectDiceBotView(idLong)) {

            val bcdice = BCDiceManager.instance
            val channel = event.channel
            val guild = event.guild

            //リアクションの確認・処理
            when (event.reactionEmote.name) {
                //ページ移動
                "⬅️" -> bcdice.backSelectDiceBotView(channel)
                "➡️" -> bcdice.nextSelectDiceBotView(channel)
                //選択
                "1️⃣" -> bcdice.select(channel, 1, guild)
                "2️⃣" -> bcdice.select(channel, 2, guild)
                "3️⃣" -> bcdice.select(channel, 3, guild)
                "4️⃣" -> bcdice.select(channel, 4, guild)
                "5️⃣" -> bcdice.select(channel, 5, guild)
                "6️⃣" -> bcdice.select(channel, 6, guild)
                "7️⃣" -> bcdice.select(channel, 7, guild)
                "8️⃣" -> bcdice.select(channel, 8, guild)
                "9️⃣" -> bcdice.select(channel, 9, guild)
                "❓" -> channel.sendMessageEmbeds(bcdice.getInfoEmbed(channel, guild)).queue()
                else -> return
            }

        }

        //クールタイムの作成
        coolTime.add(idLong)
        Executors.newSingleThreadExecutor().submit {
            try {
                Thread.sleep(100)
                coolTime.remove(idLong)
            } catch (e: InterruptedException) {
                coolTime.remove(idLong)
            }
        }
    }
}