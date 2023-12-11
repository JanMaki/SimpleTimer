package dev.simpletimer.timer

import dev.simpletimer.database.data.TimerServiceData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * タイマーのサービス
 *
 * @property seconds 秒数
 */
class TimerService(var seconds: Int, val serviceData: TimerServiceData) {
    init {
        //すでに開始している時は、コルーチンを始める
        if (serviceData.isStarted && serviceData.isMove) {
            startCoroutine()
        }
    }


    /**
     * タイマーを開始する
     *
     */
    fun start(): Boolean {
        //既に開始していたら何もしない
        if (serviceData.isStarted) {
            return false
        }

        //開始した時間を代入
        serviceData.startNanoTime = System.nanoTime()

        //コールーチンを開始する
        startCoroutine()

        //開始フラグを立てる
        serviceData.isStarted = true

        //イベントを呼び出す
        listeners.forEach { it.onStart() }

        //正常に開始したのでtrueを返す
        return true
    }

    /**
     * タイマーを一時停止する
     *
     * @return 既に一時停止していたらfalseを返す
     */
    fun stop(): Boolean {
        //既に止まっているかを確認
        val check = serviceData.isMove

        //タイマーを止める
        serviceData.isMove = false

        //停止時の時間を保存
        serviceData.stopTime = System.nanoTime()

        //イベントを呼び出す
        listeners.forEach { it.onStop(check) }

        //確認したものを返す
        return check
    }

    /**
     * タイマーを再開する
     *
     * @return 停止していなかったらfalseを返す
     */
    fun restart(): Boolean {
        //止まっているかを確認
        val check = !serviceData.isMove

        //タイマーを再開
        serviceData.isMove = true

        //止まっていたとき
        if (check) {
            //調整値を増やす
            serviceData.adjustTime += System.nanoTime() - serviceData.stopTime

            //コールーチンを開始する
            startCoroutine()
        }

        CoroutineScope(Dispatchers.Default).launch {
            delay(1)
            //イベントを呼び出す
            listeners.forEach { it.onRestart(check) }
        }

        //確認したものを返す
        return check
    }

    /**
     * タイマーを終了する
     *
     * @return 既に終了していたらfalseを返す
     */
    fun finish(): Boolean {
        //既に終わっているかを確認
        val check = !serviceData.isFinish

        //タイマーを招集
        serviceData.isFinish = true

        //イベントを呼び出す
        listeners.forEach { it.onFinish(check) }

        //確認したものを返す
        return check
    }

    /**
     * タイマーを停止する
     *
     * @return 既に終了していたらfalseを返す
     */
    fun end(): Boolean {
        //既に終わっているかを確認
        val check = !serviceData.isFinish

        //タイマーを招集
        serviceData.isFinish = true

        //イベントを呼び出す
        listeners.forEach { it.onEnd(check) }

        //確認したものを返す
        return check
    }

    /**
     * タイマーを延長する
     *
     * @param seconds 追加する秒数
     */
    fun addTimer(seconds: Int) {
        //秒数を追加
        this.seconds += seconds

        //イベントを呼び出す
        listeners.forEach { it.onAdd(seconds) }
    }

    /**
     * コールーチンを開始する
     *
     */
    private fun startCoroutine() {
        TimerCoroutineService.start(this)
    }


    //イベントを受け取るリスナー
    val listeners = ArrayList<TimerListener>()

    /**
     * リスナーを登録する
     *
     * @param listener 登録するリスナー[TimerListener]
     */
    fun registerListener(listener: TimerListener) {
        listeners.add(listener)
    }

    /**
     * リスナーのインターフェース
     *
     */
    interface TimerListener {
        /**
         * 開始したときに呼び出される
         *
         */
        fun onStart()

        /**
         * 一時停止の処理が行われた時に呼び出される
         *
         * @param check 既に止まっていたら、falseが入る
         */
        fun onStop(check: Boolean)

        /**
         * 再開の処理が行われた時に呼び出される
         *
         * @param check 止まっていなかったら、falseが入る
         */
        fun onRestart(check: Boolean)

        /**
         * 終了の処理が行われた時に呼び出される
         *
         * @param check 動作していなかったら、falseが入る
         */
        fun onFinish(check: Boolean)

        /**
         * 破棄の処理が行われた時に呼び出される
         *
         * @param check 動作していなかったら、falseが入る
         */
        fun onEnd(check: Boolean)

        /**
         * 時間が延長されたら呼び出される
         *
         * @param seconds 延長された秒数（マイナスの場合は短縮）
         */
        fun onAdd(seconds: Int)

        /**
         * 更新毎（約0.5秒）ごとに呼び出される
         *
         */
        fun onUpdate()
    }

    /**
     * 残り時間を取得する
     *
     * @return 残り時間[Time]
     */
    fun getTime(): Time {
        //経過時間
        val elapsedTime = System.nanoTime() - serviceData.startNanoTime

        //残りの秒数を取得する
        var seconds = this.seconds - ((elapsedTime - serviceData.adjustTime) / 1000000000L).toInt()
        //60で割り、小数点切り捨てで分数にする
        val minute = seconds / 60
        //分部分を除いた秒数を取得
        seconds %= 60
        //Timeにして返す
        return Time(minute, seconds)
    }

    /**
     * 分と秒をまとめたクラス
     *
     * @property minute 分[Int]
     * @property seconds 秒[Int]
     */
    data class Time(val minute: Int, val seconds: Int) {
        companion object {
            /**
             * 秒数の合計からインスタンスを作る
             * 例: 610秒 -> 10分10秒
             *
             * @param seconds 秒数
             * @return [Time]
             */
            fun getTimeFromTotalSeconds(seconds: Int): Time {
                return Time(seconds / 60, seconds % 60)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other == null) return false

            if (other !is Time) return false

            return minute == other.minute && seconds == other.seconds
        }

        override fun hashCode(): Int {
            var result = minute
            result = 31 * result + seconds
            return result
        }
    }
}