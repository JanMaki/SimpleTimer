package dev.simpletimer.listener

import dev.simpletimer.extension.checkSimpleTimerPermission
import dev.simpletimer.timer.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*

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

        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            return
        }

        //クールタイムの確認
        val idLong = event.messageIdLong
        if (coolTime.contains(idLong)) return

        //タイマーの取得
        val timer = Timer.getTimer(idLong)

        //タイマーの確認
        if (timer != null) {
            //リアクションの確認・処理
            when (event.reactionEmote.name) {
                "1️⃣" -> timer.add(1 * 60)
                "3️⃣" -> timer.add(3 * 60)
                "5️⃣" -> timer.add(5 * 60)
                "\uD83D\uDD1F" -> timer.add(10 * 60)
                else -> return
            }
        }

        //クールタイムの作成
        coolTime.add(idLong)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                delay(100)
                coolTime.remove(idLong)
            } catch (e: InterruptedException) {
                coolTime.remove(idLong)
            }
        }
    }
}