package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.Timer
import net.necromagic.simpletimerKT.bcdice.BCDiceManager

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
                        "1️⃣" -> timer.addTimer(1)
                        "3️⃣" -> timer.addTimer(3)
                        "5️⃣" -> timer.addTimer(5)
                        "\uD83D\uDD1F" -> timer.addTimer(10)
                        else -> return
                    }
                }
            }

        }

        //ダイスの確認
        else if (BCDiceManager.instance.isSelectDiceBotView(idLong)) {

            val bcdice = BCDiceManager.instance
            val textChannel = event.textChannel

            //リアクションの確認・処理
            when (event.reactionEmote.name) {
                //ページ移動
                "⬅️" -> bcdice.backSelectDiceBotView(textChannel)
                "➡️" -> bcdice.nextSelectDiceBotView(textChannel)
                //選択
                "1️⃣" -> bcdice.select(textChannel, 1)
                "2️⃣" -> bcdice.select(textChannel, 2)
                "3️⃣" -> bcdice.select(textChannel, 3)
                "4️⃣" -> bcdice.select(textChannel, 4)
                "5️⃣" -> bcdice.select(textChannel, 5)
                "6️⃣" -> bcdice.select(textChannel, 6)
                "7️⃣" -> bcdice.select(textChannel, 7)
                "8️⃣" -> bcdice.select(textChannel, 8)
                "9️⃣" -> bcdice.select(textChannel, 9)
                "❓" -> bcdice.printInfo(textChannel)
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