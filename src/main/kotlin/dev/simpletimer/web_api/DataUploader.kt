package dev.simpletimer.web_api

import com.github.kittinunf.fuel.httpPut
import dev.simpletimer.SimpleTimer
import dev.simpletimer.timer.Timer
import dev.simpletimer.web_api.data.ChannelData
import dev.simpletimer.web_api.data.TimerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.net.MalformedURLException

/**
 * タイマーのデータのアップローダー
 *
 */
class DataUploader {
    init {
        //コルーチンを開始
        CoroutineScope(Dispatchers.Default).launch {
            //直前に送ったチャンネルのID
            val sendChannels = mutableListOf<Long>()
            while (true) {
                try {
                    //直前に送ったチャンネルのデータを削除
                    sendChannels.clear()

                    //送信するリスト
                    val channelDataList = mutableListOf<ChannelData>()
                    //すべてのチャンネルを確認
                    Timer.channelsTimersMap.forEach {
                        //タイマーのリスト
                        val timers = arrayListOf<TimerData>()
                        //タイマーのデータが空だった数
                        var emptyTimerCount = 0
                        //タイマーを確認
                        it.value.apply {
                            //0~3を確認
                            for (number in 0..3) {
                                //タイマーを取得
                                val timer = this[Timer.Number.getNumber(number + 1)]
                                //nullチェック
                                if (timer == null) {
                                    //空データを入れる
                                    timers.add(TimerData())
                                    //空だったデータの数を増やす
                                    emptyTimerCount++
                                    continue
                                }
                                //タイマーサービスを取得
                                val timerService = timer.timerService
                                val time = timerService.getTime()
                                //タイマーのデータを追加
                                timers.add(
                                    TimerData(
                                        time.minute * 60 + time.seconds,
                                        System.currentTimeMillis(),
                                        timerService.isMove,
                                        timerService.isFinish
                                    )
                                )
                            }
                        }

                        //チャンネルのIDを取得
                        val id = it.key.idLong

                        //前回送っているかと空だったデータの数を確認
                        if (sendChannels.contains(id) || emptyTimerCount != 4) {
                            //チャンネルのデータを作成
                            val channelData = ChannelData(it.key.idLong, timers)

                            //送信するリストに追加
                            channelDataList.add(channelData)

                            //直前に送ったリストに追加
                            sendChannels.add(id)
                        }
                    }
                    //Post!
                    "${SimpleTimer.instance.dataContainer.config.apiURL}/timers/${SimpleTimer.instance.dataContainer.config.apiToken}"
                        .httpPut().header(hashMapOf("Content-Type" to "application/json"))
                        .body(Json.encodeToString(ListSerializer(ChannelData.serializer()), channelDataList)).response()
                } catch (ignore: MalformedURLException) {
                    ignore.printStackTrace()
                }
                //スレッドを1秒待つ
                delay(1000)
            }
        }
    }
}