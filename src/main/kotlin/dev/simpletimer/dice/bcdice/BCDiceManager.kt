package dev.simpletimer.dice.bcdice

import dev.simpletimer.SimpleTimer
import dev.simpletimer.bcdice_kt.BCDice
import dev.simpletimer.bcdice_kt.bcdice_task.GameSystem
import dev.simpletimer.component.button.DiceBotInfoButton
import dev.simpletimer.component.button.DiceBotPageButton
import dev.simpletimer.component.select_menu.DiceBotSelectMenu
import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.*
import dev.simpletimer.util.Log
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.ActionRow
import java.awt.Color

class BCDiceManager {
    companion object {
        lateinit var instance: BCDiceManager
    }

    //ゲームシステムの一覧をページで分ける
    private val gameSystemPages = HashMap<Int, ArrayList<GameSystem>>()

    private val bcdice = BCDice()

    init {
        instance = this

        if (!bcdice.wasInstalled()) {
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

    /**
     * ダイス選択画面のEmbedを作成する
     *
     * @param page [Int]　ページ
     * @return [MessageEmbed] メッセージの埋め込み
     */
    private fun createView(langData: LangData, page: Int): MessageEmbed? {
        //ページを取得
        val gameSystems = gameSystemPages[page] ?: return null

        //作成開始
        val builder = EmbedBuilder()
        builder.setTitle(langData.dice.bcDice.botSelect, "https://bcdice.org/systems/")
        builder.setDescription(langData.dice.bcDice.botSelectDescription)
        builder.setColor(Color.BLUE)

        //ページ内のgameSystemを貼り付けていく
        for ((count, gameSystem) in gameSystems.withIndex()) {
            val emoji = (count + 1).emoji
            builder.addField(emoji, "**${gameSystem.name}**", false)
        }

        //ページ数の表示
        builder.setFooter(langData.dice.bcDice.page.langFormat(page, gameSystemPages.size))

        return builder.build()
    }

    /**
     * ダイス選択画面を送る
     *
     * @param channel [GuildMessageChannel] 対象のチャンネル
     */
    fun openSelectDiceBotView(channel: GuildMessageChannel) {
        //embedを作成
        val embed = createView(channel.guild.getLang(), 1) ?: return

        //メッセージを送信
        channel.sendMessageEmbeds(embed).setComponents(
            ActionRow.of(DiceBotSelectMenu.createSelectMenu(gameSystemPages[1]!!, channel.guild.getLang())),
            ActionRow.of(
                DiceBotPageButton.BackButton.createButton(0, channel.guild.getLang()),
                DiceBotPageButton.NextButton.createButton(2, channel.guild.getLang())
            )
        ).queue()
    }

    /**
     * ボタンに応答してページを変更する
     *
     * @param event [ButtonInteractionEvent]ボタンのイベント
     */
    fun changePageFromButtonInteraction(event: ButtonInteractionEvent) {
        //ボタンのIDを取得
        val buttonID = event.button.id ?: return

        //ページを取得
        val page = buttonID.split(":")[1].toInt()

        //ページの上下限を確認
        if (page < 1 || page > gameSystemPages.size) {
            //空白を送信
            event.deferReply().queue {
                event.hook.sendEmpty()
            }
            return
        }

        //メッセージを更新
        event.message.editMessageEmbeds(createView(event.guild!!.getLang(), page)).queue {
            //コンポーネント類を編集
            event.editComponents(
                ActionRow.of(DiceBotSelectMenu.createSelectMenu(gameSystemPages[page]!!, event.guild!!.getLang())),
                ActionRow.of(
                    DiceBotPageButton.BackButton.createButton(page - 1, event.guild!!.getLang()),
                    DiceBotPageButton.NextButton.createButton(page + 1, event.guild!!.getLang())
                ),
            ).queue()
        }
    }

    /**
     * 選択メニューに応じてBotを選択
     *
     * @param event [SelectMenuInteractionEvent]対象のイベント
     */
    fun selectFromInteraction(event: IReplyCallback, id: String) {
        //チャンネルを取得1
        val channel = event.guildChannel as GuildMessageChannel

        try {
            //ゲームシステムを取得
            val gameSystem = bcdice.getGameSystem(id)

            //言語のデータ
            val langData = channel.guild.getLang()

            //メッセージを送信
            event.hook.sendMessage(
                langData.dice.bcDice.changeDiceBot.langFormat(
                    "**${gameSystem.name}**",
                    gameSystem.id
                )
            )
                .setComponents(ActionRow.of(DiceBotInfoButton.createButton(0, channel.guild.getLang()))).queue()

            //ギルドのデータを取得
            val guildData = channel.guild.getGuildData()

            //ダイスモードを自動的に変更する
            if (guildData.diceMode == DiceMode.Default) {
                event.hook.sendMessage(langData.dice.changeDiceMode.langFormat("**BCDice**")).queue()
            }

            //データを保存
            guildData.diceBot = gameSystem.id
            guildData.diceMode = DiceMode.BCDice
            SimpleTimer.instance.dataContainer.saveGuildsData(channel.guild)
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)) {
                return
            }
            //Botが見つからなかった時
            if (e is java.lang.IllegalArgumentException) {
                event.hook.sendMessage(event.guild!!.getLang().dice.bcDice.unknownBot).queue()
                return
            }
            Log.sendLog(e.stackTraceToString(), true)
        }
    }

    /**
     * ダイスの説明画面を表示
     *
     * @param channel [GuildMessageChannel] 送信するテキストチャンネル
     * @param guild [Guild] 対象のギルド
     */
    fun getInfoEmbed(channel: GuildMessageChannel, guild: Guild): MessageEmbed {
        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //言語のデータ
        val langData = guild.getLang()

        //ギルドのデータから設定されているダイスボットを取得
        val id = guildData.diceBot

        //ダイスボットの詳細を取得
        val gameSystemInfo = bcdice.getGameSystem(id)

        //作成開始
        var builder = EmbedBuilder()
        builder.setTitle(gameSystemInfo.name)
        builder.setDescription(langData.dice.bcDice.diceDescription)
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
                    builder.addField(langData.dice.bcDice.gameCommand, buffer.toString(), false)
                    isFirst = false
                } else {
                    builder.addField(" ", buffer.toString(), false)
                }

                channel.sendMessageEmbeds(builder.build()).queue()
                buffer.clear()

                builder = EmbedBuilder()
                builder.setColor(Color.BLUE)
            }
        } else {
            builder.addField(langData.dice.bcDice.gameCommand, gameSystemInfo.help_message, false)
        }

        //システム共通を張る
        if (id != "DiceBot") {
            val defaultGameSystem = bcdice.getGameSystem("DiceBot")
            builder.addField(langData.dice.bcDice.commonCommand, defaultGameSystem.help_message, false)
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

        //ギルドのデータから設定されているダイスボットを取得
        val id = guild.getGuildData().diceBot

        //ダイスボットの詳細を取得
        val gameSystem = bcdice.getGameSystem(id)

        //コマンドのパターンを取得
        val result = gameSystem.roll(runCommand)

        //失敗時はnullを返す
        if (!result.check) {
            return null
        }

        //メッセージを構築して返す
        return if (result.secret) {
            //シークレットダイスダイス
            "${guild.getLang().dice.secret}\n||${result.text}||"
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

    /**
     * GameSystemの一覧を取得する
     *
     * @return リストで返す
     */
    fun getGameSystems(): List<GameSystem> {
        return gameSystemPages.values.flatten()
    }
}