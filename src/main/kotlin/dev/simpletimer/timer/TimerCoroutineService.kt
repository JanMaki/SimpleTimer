package dev.simpletimer.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * タイマーのコールーチン
 */
class TimerCoroutineService(timerService: TimerService) {
    companion object {
        //コールーチンの一覧
        private val coroutineServices = arrayListOf<TimerCoroutineService>()

        /**
         * [TimerService]のコールーチンを開始する
         *
         * @param timerService [TimerService]
         */
        fun start(timerService: TimerService) {
            //コールーチンがすでになかったり、最後のコールーチン内で処理している数が多いかを確認
            if (coroutineServices.isEmpty() || coroutineServices.last()
                    .getTimerCount() > 19 || !coroutineServices.last().active
            ) {
                //新しくサービスを作る
                coroutineServices.add(TimerCoroutineService(timerService))
                return
            }

            //最後のコールーチンを取得
            val coroutineService = coroutineServices.last()

            //すでにあるコールーチンに登録をする
            coroutineService.registerTimerService(timerService)
        }
    }

    //動かすタイマーサービス
    private val timers = arrayListOf(timerService)

    //有効か
    var active = true
        private set

    init {
        //別スレッドでタイマーを開始
        CoroutineScope(Dispatchers.Default).launch {
            do {
                ArrayList<TimerService>(timers).forEach { timer ->
                    //経過時間を更新
                    timer.elapsedTime = System.nanoTime() - timer.startNanoTime

                    //離脱フラグ
                    var leave = false

                    //停止のフラグを確認
                    if (!timer.isMove) {
                        //停止時の時間を保存
                        timer.stopTime = System.nanoTime()
                        //離脱する
                        leave = true
                    }
                    //時間を確認
                    else if (timer.elapsedTime >= (timer.seconds * 1000000000L) + timer.adjustTime) {
                        //終了処理を実行
                        timer.finish() //離脱する
                        leave = true

                    }
                    //終了フラグを確認
                    else if (timer.isFinish) {
                        //離脱する
                        leave = true
                    }

                    //離脱を確認
                    if (leave) {
                        //削除
                        timers.remove(timer)
                        //戻る
                        return@forEach
                    }

                    //イベントを呼び出す
                    timer.listeners.forEach { it.onUpdate() }
                }

                //スレッドを0.5秒待つ
                delay(500)
            } while (timers.size > 0)

            //無効にする
            active = false
        }
    }

    /**
     * [TimerService]を追加
     *
     * @param timerService [TimerService]
     */
    fun registerTimerService(timerService: TimerService) {
        timers.add(timerService)
    }

    /**
     * 動かしているタイマーの数を取得
     *
     * @return 数[Int]
     */
    fun getTimerCount(): Int {
        return timers.size
    }
}