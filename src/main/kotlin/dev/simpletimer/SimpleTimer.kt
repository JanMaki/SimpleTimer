package dev.simpletimer

import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.DataContainer
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.listener.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import java.io.IOException
import java.net.ServerSocket
import java.net.URL
import java.util.*

// v1.0.0 リリース
// v1.0.1 リアクションによる操作の実装
// v1.0.2 メッセージ消去時のエラーを修正
// v1.0.3 権限がないときにDMへの通知を行うように
// v1.0.4 並列処理の見直し
// v1.1.0 メインメッセージ消去時の反応を調整
// v1.1.1 リアクションに1分延長を追加
// v1.2.0 メンションの方式の変更
// v1.2.1 エラーメッセージのバグを修正。誤字の修正。
// v1.2.2 vcにいるbotへのメンションをしないように変更。
// v1.2.3 メンションのデフォルトをHere->VC
// v1.2.4 1番タイマーのデザインを変更
// v1.2.5 !!timerでも反応をするように変更
// v1.2.6 すべてのコードをJava->Kotlinに書き直し
// v1.2.7 無駄な改行が入っていたのを修正
// v1.2.8 隠し機能：ダイスの実装
// v1.2.9 Prefixの変更機能を実装
// v1.2.10 一部コードの見直し
// v1.2.11 並列処理の見直しその2
// v1.3.0 APIの更新 通知の送信を返信で行うように変更
// v1.3.1 メッセージのメンションの位置を下に移動
// v1.3.2 一部負荷をかけることができる動作を制限
// v1.3.3 スポイラーされたダイスをシークレットダイスに
// v1.3.3 ダイスボットをBCDiceに仮対応
// v1.4.0 スラッシュコマンドに対応
// v1.4.1 BCDiceの使用時は、スペース後を無視するように
// v1.4.2 プレフィックスが変更できなくなるバグの修正
// v1.4.3 タイマーのTTS方式を変更
// v1.4.4 誤字の修正
// v1.4.5 JDAのバージョンを更新
// v1.4.6 スラッシュコマンドの正式実装
// v1.4.7 時間をより正確に調整
// v1.4.8 一時停止が一部動いていなかったのを修正
// v1.4.9 ダイスのInfoの文字数が多いと表示されなかったのを修正
// v1.4.10 tokenを外部ファイル(server_config.yml)にて記述するように変更
// v1,4,11 シャードを使用するように変更
// v1.5.0 スラッシュコマンドを一新
// v1.5.1 一覧機能を実装
// v1.5.2 メンション方式の追加
// v1.5.3 JDAのバージョンを更新
// v1.5.4 メンションの方式を改修
// v1.5.5 一覧の同期を実装
// v2.0.0 https://source.simpletimer.dev/pull/1


/**
 * メインクラス
 *
 */
class SimpleTimer {
    companion object {
        lateinit var instance: SimpleTimer

        /**
         * すべての始まり
         *
         * @param args [Array] 引数（未使用）
         */
        @JvmStatic
        fun main(args: Array<String>) {
            SimpleTimer()
        }

    }

    //バージョン
    val version = "v2.0.0"

    //多重起動防止
    private val lockPort = 918
    private lateinit var socket: ServerSocket

    //データ保存用
    lateinit var dataContainer: DataContainer

    //起動しているShardのSet
    val shards = mutableSetOf<JDA>()

    init {
        init()
    }

    /**
     * 起動時処理
     *
     */
    private fun init() {
        //インターネットの接続の確認
        try {
            val url = URL("https://google.com")
            val connection = url.openConnection()
            connection.getInputStream().close()
        } catch (ioException: IOException) {
            try {
                Thread.sleep(5000)
                init()
            } catch (interruptedException: InterruptedException) {
                interruptedException.printStackTrace()
            }
            return
        }

        //多重起動の防止
        var check = true
        try {
            socket = ServerSocket(lockPort)
        } catch (e: Exception) {
            check = false
        }
        if (!check) {
            return
        }

        //インスタンスを代入
        instance = this

        //データのクラス
        dataContainer = DataContainer()

        //BCDiceのマネージャーを開始
        BCDiceManager()

        //Tokenを取得
        val token = dataContainer.config.token

        //トークンがないときに終了する
        if (token.equals("TOKEN IS HERE", ignoreCase = true)) {
            //コンソールに出力
            println("SETUP: Write the token in the \"token\" field of config.yml")
            return
        }

        //JDAを作成
        val shardBuilder = JDABuilder.createDefault(token)

        //リスナーの登録
        shardBuilder.addEventListeners(ButtonInteraction())
        shardBuilder.addEventListeners(GenericMessageReaction())
        shardBuilder.addEventListeners(MessageDelete())
        shardBuilder.addEventListeners(Ready())
        shardBuilder.addEventListeners(SlashCommandInteraction())
        shardBuilder.addEventListeners(SelectMenuInteraction())

        shardBuilder.setStatus(OnlineStatus.ONLINE)
        shardBuilder.setActivity(Activity.of(Activity.ActivityType.PLAYING, "!timerでヘルプ表示"))

        for (i in 0..2) {
            val shard = shardBuilder.useSharding(i, 3).build()

            shards.add(shard)

            shard.updateCommands().addCommands(SlashCommandManager.slashCommands).queue({}, {})
        }

        //終了処理
        //入力を作成
        var input: String?
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            //入力を取得
            input = scanner.nextLine()
            //終了の場合
            if (input == "exit") {
                //JDAを終了
                shards.forEach {
                    it.shutdown()
                }
                println("Botを終了します...")
                break
            }
        }
    }

    fun getGuild(id: Long): Guild? {
        var guild: Guild? = null
        shards.forEach {
            if (guild != null) {
                return@forEach
            }
            guild = it.getGuildById(id)
        }
        return guild
    }
}