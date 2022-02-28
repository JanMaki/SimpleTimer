package net.necromagic.simpletimer.listener

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimer.dice.Dice
import net.necromagic.simpletimer.timer.Timer
import java.util.*

class ButtonClick:ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        super.onButtonInteraction(event)
        event.deferReply().queue()

        val buttonID = event.componentId
        val channel = event.channel

        //タイマーのボタン
        if (buttonID.contains("timer")){
            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始・代入
                    channelTimers[number] =  Timer(
                        channel,
                        number,
                        buttonID.replace("timer-","").toIntOrNull() ?: return
                        , event.guild!!
                    )
                    Timer.channelsTimersMap[channel] = channelTimers
                    //空白を送信
                    event.hook.sendMessage("|| ||").queue { message ->
                        message.delete().queue()
                    }
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）").queue()
        }

        //ダイスのボタン
        if(buttonID.contains("dice")){
            //ダイスのコマンドを取得
            val value = buttonID.replace("dice-","")

            //ダイスを振る
            Dice().roll(event, value, event.user)
        }
    }
}