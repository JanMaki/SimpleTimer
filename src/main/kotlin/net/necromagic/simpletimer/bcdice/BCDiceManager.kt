package net.necromagic.simpletimer.bcdice

import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.GameSystem
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.necromagic.simpletimer.*
import net.necromagic.simpletimer.util.Log
import net.necromagic.simpletimer.util.equalsIgnoreCase
import java.awt.Color
import java.lang.StringBuilder

class BCDiceManager {
    companion object {
        lateinit var instance: BCDiceManager
    }

    //ゲームシステムの一覧をページで分ける
    private val gameSystemPages = HashMap<Int, ArrayList<GameSystem>>()

    private val bcdice = BCDice()

    init {
        instance = this

        if (!bcdice.wasInstalled()){
            bcdice.install()
        }

        bcdice.setup()

        //ゲームシステムの一覧を取得
        val gameSystems = bcdice.getGameSystems()!!

        //ページで分ける
        var page = 1
        var count = 1
        for (gameSystem in gameSystems) {
            val gameSystemPage = gameSystemPages.getOrDefault(page, ArrayList())
            gameSystemPage.add(gameSystem)
            gameSystemPages[page] = gameSystemPage

            //１ページ９個言ったら次のページへ
            if (count == 9) {
                count = 1
                page++
            } else {
                count++
            }
        }
    }

    //番号の絵文字の配列
    private val numberEmojis = arrayOf("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣")

    /**
     * ダイス選択画面のEmbedを作成する
     *
     * @param page [Int]　ページ
     * @return [MessageEmbed] メッセージの埋め込み
     */
    private fun createView(page: Int): MessageEmbed? {
        //ページを取得
        val gameSystems = gameSystemPages[page] ?: return null

        //作成開始
        val builder = EmbedBuilder()
        builder.setTitle("ダイスBot選択", "https://bcdice.org/systems/")
        builder.setDescription("使用するダイスボットを選択してください")
        builder.setColor(Color.BLUE)

        //ページ内のgameSystemを貼り付けていく
        for ((count, gameSystem) in gameSystems.withIndex()) {
            val emoji = numberEmojis[count]
            builder.addField(emoji, "**${gameSystem.name}**", false)
        }

        //ページ数の表示
        builder.setFooter("ページ: ${page}/${gameSystemPages.size}")

        return builder.build()
    }

    //チャンネルに送ってるViewのマップ
    private val channelViews = HashMap<MessageChannel, Message>()

    //チャンネルに送ってるViewの開いているページ
    private val channelViewsPage = HashMap<MessageChannel, Int>()

    /**
     * ダイス選択画面を送る
     *
     * @param channel [MessageChannel] 対象のチャンネル
     */
    fun openSelectDiceBotView(channel: MessageChannel) {
        //embedを作成
        val embed = createView(1) ?: return
        try {
            //過去の物を消す
            if (channelViews.containsKey(channel)) {
                channelViews[channel]?.delete()?.complete()
            }
            //メッセージを送信
            val message = channel.sendMessageEmbeds(embed).complete()
            //マップに登録
            channelViews[channel] = message
            //ページを初期化
            channelViewsPage[channel] = 1
            //リアクションを付与
            message.addReaction("⬅️").queue({}, {})
            for (emoji in numberEmojis) {
                message.addReaction(emoji).queue({}, {})
            }
            message.addReaction("➡️").queue({}, {})
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)){
                return
            }
            Log.sendLog(e.stackTraceToString())
        }
    }

    /**
     * ダイス選択画面のページを進める
     *
     * @param channel [MessageChannel] 該当のチャンネル
     */
    fun nextSelectDiceBotView(channel: MessageChannel) {
        //開いているページを取得
        val page = channelViewsPage[channel] ?: return
        //埋め込みを作成
        val embed = createView(page + 1) ?: return
        //ページの変数を増やす
        channelViewsPage[channel] = page + 1
        //対象のメッセージを取得
        val message = channelViews[channel] ?: return
        //編集
        message.editMessageEmbeds(embed).queue({}, {})
    }

    /**
     * ダイス選択画面のページを戻す
     *
     * @param channel [MessageChannel] 該当のチャンネル
     */
    fun backSelectDiceBotView(channel: MessageChannel) {
        //開いているページを取得
        val page = channelViewsPage[channel] ?: return
        //埋め込みを作成
        val embed = createView(page - 1) ?: return
        //ページの変数を減らす
        channelViewsPage[channel] = page - 1
        //対象のメッセージを取得
        val message = channelViews[channel] ?: return
        //編集
        message.editMessageEmbeds(embed).queue({}, {})
    }

    /**
     * メッセージのIDから、選択画面かを確認する
     *
     * @param message [Long] メッセージのID
     * @return [Boolean] 選択画面か
     */
    fun isSelectDiceBotView(message: Long): Boolean {
        channelViews.forEach { entry ->
            val value = entry.value
            if (value.idLong == message) {
                return true
            }
        }
        return false
    }

    /**
     * ダイスボットを選択する
     *
     * @param channel [MessageChannel] 該当のテキストチャンネル
     * @param slot [Int] 開いてるページの何番目か
     * @param guild [Guild] 対象のギルド
     */
    fun select(channel: MessageChannel, slot: Int, guild:Guild) {
        //開いているページを取得
        val pageNumber = channelViewsPage[channel] ?: return
        //ページ内のゲームシステムを取得
        val gameSystems = gameSystemPages[pageNumber] ?: return
        //ゲームシステムを取得
        val gameSystem = gameSystems[slot - 1]
        try {
            //メッセージを送信
            val selectMessage = channel.sendMessage("ダイスBotを**${gameSystem.name}**に変更しました").complete()
            selectMessage.addReaction("❓").queue({}, {})

            //コンフィグのインスタンス
            val config = SimpleTimer.instance.config

            //ダイスモードを自動的に変更する
            if (config.getDiceMode(guild) == ServerConfig.DiceMode.Default) {
                channel.sendMessage("ダイスモードを**BCDice**に変更しました id:${gameSystem.id}").queue({}, {})
            }

            config.setDiceBot(guild, gameSystem.id)
            config.setDiceMode(guild, ServerConfig.DiceMode.BCDice)
            config.save()
            //設定画面を消す
            if (channelViews.containsKey(channel)) {
                channelViews[channel]?.delete()?.complete()
            }
            channelViews[channel] = selectMessage
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)){
                return
            }
            Log.sendLog(e.stackTraceToString())
        }
    }

    /**
     * ダイスの説明画面を表示
     *
     * @param channel [MessageChannel] 送信するテキストチャンネル
     * @param guild [Guild] 対象のギルド
     */
    fun getInfoEmbed(channel: MessageChannel, guild: Guild): MessageEmbed {
        val prefix = SimpleTimer.instance.config.getPrefix(guild)

        //コンフィグから設定されているダイスボットを取得
        val id = SimpleTimer.instance.config.getDiceBot(guild)

        //ダイスボットの詳細を取得
        val gameSystemInfo = bcdice.getGameSystem(id)!!

        //作成開始
        var builder = EmbedBuilder()
        builder.setTitle(gameSystemInfo.name)
        builder.setDescription("ダイスは\"${prefix}+コマンド\"で実行できます 例: ${prefix}1D100")
        builder.setColor(Color.BLUE)

        if (gameSystemInfo.help_message.length >= 1024) {
            val helpArray = gameSystemInfo.help_message.split("\n")
            var index = 0
            val buffer = StringBuilder()
            var isFirst = true

            while (index < helpArray.size) {
                while (index < helpArray.size && buffer.toString().length + helpArray[index].length + 2 <= 1024) {
                    buffer.append("\n")
                    buffer.append(helpArray[index])
                    index++
                }

                if (isFirst) {
                    builder.addField("ゲーム固有コマンド", buffer.toString(), false)
                    isFirst = false
                } else {
                    builder.addField(" ", buffer.toString(), false)
                }

                channel.sendMessageEmbeds(builder.build()).queue({}, {})
                buffer.clear()

                builder = EmbedBuilder()
                builder.setColor(Color.BLUE)
            }
        } else {
            builder.addField("ゲーム固有コマンド", gameSystemInfo.help_message, false)
        }

        //システム共通を張る
        if (id != "DiceBot") {
            val defaultGameSystem = bcdice.getGameSystem("DiceBot")
            builder.addField("システム共通コマンド", defaultGameSystem?.help_message, false)
        }

        return builder.build()
    }

    /**
     * ダイスを振る
     *
     * @param guild [Guild] ダイスを振るギルド
     * @param command [String] ダイスの内容
     */
    fun roll(guild: Guild, command: String): String? {
        var runCommand = command

        //スポイラーによるシークレットダイスを確認
        if (command.contains("||") && !command.substring(0, 1).equalsIgnoreCase("s")) {
            runCommand = "s$command".replace("||", "")
        }

        //コンフィグから設定されているダイスボットを取得
        val id = SimpleTimer.instance.config.getDiceBot(guild)

        //ダイスボットの詳細を取得
        val gameSystem = bcdice.getGameSystem(id) ?: return null

        //コマンドのパターンを取得
        val result = gameSystem.roll(runCommand)

        //失敗時はnullを返す
        if (!result.check){
            return null
        }

        //メッセージを構築して返す
        return if (result.secret) {
            //シークレットダイスダイス
            "(シークレットダイス)\n||${result.text}||"
        } else {
            if (result.success || result.critical) {
                //成功
                "```md\n# ${result.text}```"
            } else if (result.failure || result.fumble) {
                //失敗
                "```cs\n# ${result.text}```"
            } else {
                //その他
                result.text
            }
        }
    }
}