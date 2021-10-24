package net.necromagic.simpletimerKT.listener

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimerKT.SimpleTimer
import net.necromagic.simpletimerKT.Timer
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
            var long = SimpleTimer.instance.config.getTimerChannelID(event.guild!!)
            //設定されていなかったら、実行されたチャンネルのIDを対象にする
            if (long < 0) long = event.channel.idLong

            //チャンネルを取得
            val channel = guild.getTextChannelById(long) ?: return

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始
                    val timer = Timer(channel, number, minutes)

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