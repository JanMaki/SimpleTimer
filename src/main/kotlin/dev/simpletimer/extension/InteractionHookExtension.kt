package dev.simpletimer.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.interactions.InteractionHook

//InteractionHookを拡張する


/**
 * 空白を送信する
 *
 */
fun InteractionHook.sendEmpty() {
    this.sendMessage("|| ||").queue {
        CoroutineScope(Dispatchers.Default).launch {
            //1ms止める
            delay(1)
            //削除
            it.delete().queue()
        }
    }
}