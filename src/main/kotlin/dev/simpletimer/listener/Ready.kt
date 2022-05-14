package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.util.Log
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Shardの開始に対応する
 *
 */
class Ready : ListenerAdapter() {
    private var count = 0

    /**
     * JDA(Shardも含む)起動時に呼び出される
     *
     * @param event [ReadyEvent]イベント
     */
    override fun onReady(event: ReadyEvent) {
        //コンフィグを取得
        val config = SimpleTimer.instance.dataContainer.config

        //サーバーの一覧を確認
        config.loggingChannels.forEach guildID@{
            //チャンネルを取得
            val channel = event.jda.getTextChannelById(it) ?: return@guildID
            //ログを出力するチャンネルに追加
            Log.logChannels.add(channel)
        }

        Log.sendLog("Shardを起動しました　${++count}/${SimpleTimer.instance.dataContainer.config.shardsCount}")

        //最後のShardじゃなかったらおしまい
        if (count != SimpleTimer.instance.dataContainer.config.shardsCount) return

        //ギルドのデータを読み込み
        SimpleTimer.instance.dataContainer.loadGuild()
    }
}