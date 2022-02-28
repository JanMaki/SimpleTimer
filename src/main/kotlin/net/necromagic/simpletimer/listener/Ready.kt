package net.necromagic.simpletimer.listener

import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.util.Log

class Ready : ListenerAdapter() {
    /**
     * JDA(Shardも含む)起動時に呼び出される
     *
     * @param event [ReadyEvent]イベント
     */
    override fun onReady(event: ReadyEvent) {
        //コンフィグを取得
        val config = SimpleTimer.instance.config

        //ログを出力するサーバーの一覧を取得
        val section = config.getConfigurationSection("LoggingServer")
        //サーバーの一覧を確認
        section?.getKeys(false)?.forEach guildID@{ guildID ->

            //チャンネルを取得
            val channel = event.jda.getTextChannelById(config.getString("LoggingServer.${guildID}")) ?: return@guildID
            //ログを出力するチャンネルに追加
            Log.logChannels.add(channel)

        }
    }
}