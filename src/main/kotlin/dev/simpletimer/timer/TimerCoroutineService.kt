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
                    timerService.elapsedTime = System.nanoTime() - timerService.startNanoTime

                    //離脱フラグ
                    var leave = false

                    //停止のフラグを確認
                    if (!timerService.isMove) {
                        //停止時の時間を保存
                        timerService.stopTime = System.nanoTime()
                        //離脱する
                        leave = true
                    }
                    //時間を確認
                    else if (timerService.elapsedTime >= (timerService.seconds * 1000000000L) + timerService.adjustTime) {
                        //終了処理を実行
                        timerService.finish() //離脱する
                        leave = true

                    }
                    //終了フラグを確認
                    else if (timerService.isFinish) {
                        //離脱する
                        leave = true
                    }

                    //離脱を確認
                    if (leave) {
                        //削除
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