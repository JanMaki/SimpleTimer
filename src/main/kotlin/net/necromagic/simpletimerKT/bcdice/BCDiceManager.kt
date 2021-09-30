package net.necromagic.simpletimerKT.bcdice

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.necromagic.simpletimerKT.*
import net.necromagic.simpletimerKT.bcdice.dataclass.*
import net.necromagic.simpletimerKT.util.Log
import net.necromagic.simpletimerKT.util.equalsIgnoreCase
import java.awt.Color
import java.lang.StringBuilder

class BCDiceManager {
    companion object {
        lateinit var instance: BCDiceManager
    }

    //APIサーバーのURL
    private val server = "https://bcdice.onlinesession.app/v2/"

    //ゲームシステムの一覧をページで分ける
    private val gameSystemPages = HashMap<Int, ArrayList<GameSystem>>()


    init {
        instance = this

        //ゲームシステムの一覧を取得
        val response = "${server}game_system".httpGet().response()
        val gameSystems = Json.decodeFromString(GameSystemArray.serializer(), String(response.second.data)).game_system

        //ページで分ける
        var page = 1
        var count = 1
        for (gameSystem in gameSystems) {
            gameSystem.page = page
            gameSystem.number = count

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
    private val channelViews = HashMap<TextChannel, Message>()

    //チャンネルに送ってるViewの開いているページ
    private val channelViewsPage = HashMap<TextChannel, Int>()

    /**
     * ダイス選択画面を送る
     *
     * @param channel [TextChannel] 対象のチャンネル
     */
    fun openSelectDiceBotView(channel: TextChannel) {
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
            Log.sendLog(e.stackTraceToString())
        }
    }

    /**
     * ダイス選択画面のページを進める
     *
     * @param channel [TextChannel] 該当のチャンネル
     */
    fun nextSelectDiceBotView(channel: TextChannel) {
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
     * @param channel [TextChannel] 該当のチャンネル
     */
    fun backSelectDiceBotView(channel: TextChannel) {
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
     * @param channel [TextChannel] 該当のテキストチャンネル
     * @param slot [Int] 開いてるページの何番目か
     */
    fun select(channel: TextChannel, slot: Int) {
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
            if (config.getDiceMode(channel.guild) == ServerConfig.DiceMode.Default) {
                channel.sendMessage("ダイスモードを**BCDice**に変更しました id:${gameSystem.id}").queue({}, {})
            }

            config.setDiceBot(channel.guild, gameSystem.id)
            config.setDiceMode(channel.guild, ServerConfig.DiceMode.BCDice)
            config.save()
            //設定画面を消す
            if (channelViews.containsKey(channel)) {
                channelViews[channel]?.delete()?.complete()
            }
            channelViews[channel] = selectMessage
        } catch (e: Exception) {
            Log.sendLog(e.stackTraceToString())
        }
    }

    /**
     * ダイスの説明画面を表示
     *
     * @param channel [TextChannel] 送信するテキストチャンネル
     */
    fun getInfoEmbed(channel: TextChannel): MessageEmbed {
        val prefix = SimpleTimer.instance.config.getPrefix(channel.guild)

        //コンフィグから設定されているダイスボットを取得
        val id = SimpleTimer.instance.config.getDiceBot(channel.guild)

        //ダイスボットの詳細を取得
        val gameSystemInfoResponse = "${server}game_system/$id".httpGet().response()
        val gameSystemInfo =
            Json.decodeFromString(GameSystemInfo.serializer(), String(gameSystemInfoResponse.second.data))

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
            val defaultGameSystemInfoResponse = "${server}game_system/DiceBot".httpGet().response()
            val defaultGameSystemInfo =
                Json.decodeFromString(GameSystemInfo.serializer(), String(defaultGameSystemInfoResponse.second.data))
            builder.addField("システム共通コマンド", defaultGameSystemInfo.help_message, false)
        }

        return builder.build()
    }

    private val json = Json{ ignoreUnknownKeys = true }

    /**
     * ダイスを振る
     *
     * @param channel [TextChannel] ダイスを振るチャンネル
     * @param command [String] ダイスの内容
     */
    fun roll(channel: TextChannel, command: String): String? {
        var runCommand = command

        //スポイラーによるシークレットダイスを確認
        if (command.contains("||") && !command.substring(0, 1).equalsIgnoreCase("s")) {
            runCommand = "s$command".replace("||", "")
        }

        //コンフィグから設定されているダイスボットを取得
        val id = SimpleTimer.instance.config.getDiceBot(channel.guild)

        //ダイスボットの詳細を取得
        val gameSystemInfoResponse = "${server}game_system/$id".httpGet().response()
        val gameSystemInfo =
            Json.decodeFromString(GameSystemInfo.serializer(), String(gameSystemInfoResponse.second.data))

        //コマンドのパターンを取得
        val commandPattern = Regex(gameSystemInfo.command_pattern, RegexOption.IGNORE_CASE)
        val result: DiceRoll

        //コマンドの確認
        if (commandPattern.containsMatchIn(runCommand)) {

            //コマンドを送信する
            val rollPost = "${server}game_system/${id}/roll".httpPost(listOf("command" to runCommand)).response()

            //正常に処理されたかを確認
            val check = json.decodeFromString(OK.serializer(), String(rollPost.second.data))
            if (check.ok) {
                //正常だった場合、結果を代入する
                result = json.decodeFromString(DiceRoll.serializer(), String(rollPost.second.data))
            } else {
                return null
            }
        } else {
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