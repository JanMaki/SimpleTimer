package dev.simpletimer.extension

import net.dv8tion.jda.api.interactions.InteractionHook

//InteractionHookを拡張する


/**
 * 空白を送信する
 *
 */
fun InteractionHook.sendEmpty() {
    this.sendMessage("|| ||").queue()
}