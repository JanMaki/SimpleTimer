package dev.simpletimer.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * タイマーのコールーチン
 */
class TimerCoroutineService {
    companion object {
        /**
         * [TimerService]のコールーチンを開始する
         *
         * @param timerService [TimerService]
         */
        fun start(timerService: TimerService) {
            //別スレッドでタイマーを開始
            CoroutineScope(Dispatchers.Default).launch {
                do {
                    //経過時間を更新
                    val elapsedTime = System.nanoTime() - timerService.serviceData.startNanoTime

                    //動いていないときは離脱
                    if (!timerService.serviceData.isMove) break

                    //終了のフラグが立っているときも離脱
                    else if (timerService.serviceData.isFinish) break

                    //時間が経過しているとき
                    else if (elapsedTime >= (timerService.seconds * 1000000000L) + timerService.serviceData.adjustTime) {
                        //終了処理を実行
                        timerService.finish()
                        //離脱
                        break
                    }

                    //イベントを呼び出す
                    timerService.listeners.forEach { it.onUpdate() }
                    //スレッドを0.5秒待つ
                    delay(500)
                } while (true)
            }
        }
    }
}