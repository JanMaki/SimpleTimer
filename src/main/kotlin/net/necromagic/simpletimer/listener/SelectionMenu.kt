package net.necromagic.simpletimer.listener

import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.timer.Timer
import java.util.*

class SelectionMenu : ListenerAdapter() {
    override fun onSelectionMenu(event: SelectionMenuEvent) {
        //実行されたサーバー
        val guild = event.guild!!

        //実行されたメニュー
        val selectionMenu = event.selectionMenu ?: return

        //メニューのID
        val id = selectionMenu.id ?: return

        //選ばれたオプション
        val options = event.selectedOptions ?: return

        //タイマーリストの時の処理
        if (id == "TimerList") {
            //とりあえず待たせる
            event.deferReply().queue()

            //オプションを取得
            val option = options[0] ?: return

            //':'で分裂させる
            val splitted = option.value.split(":")

            //分を取得
            val minutes = splitted[1].toInt()


            //コンフィグからチャンネルのIDを取得
            val long = SimpleTimer.instance.config.getTimerChannelID(event.guild!!)

            //チャンネルを取得
            var channel: MessageChannel? = guild.getTextChannelById(long) ?: guild.getThreadChannelById(long)

            if (channel == null){
                channel = event.channel
            }

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始
                    val timer = Timer(channel, number, minutes, event.guild!!)

                    //タイマーのインスタンスを代入する
                    channelTimers[number] = timer
                    Timer.channelsTimersMap[channel] = channelTimers


                    //空白を出力して消し飛ばす
                    event.hook.sendMessage(number.format("${splitted[0]}（${minutes}分）を実行しました")).complete()
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）").queue()
            return
        }

        event.reply("").complete()
    }
}