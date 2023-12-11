package dev.simpletimer.component.select_menu

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.dice.Dice
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

/**
 * 一覧の選択メニュー
 *
 */
object ListSelectMenu : SelectMenuManager.SelectMenu<LinkedHashMap<String, String>>("list", false) {
    override fun run(event: StringSelectInteractionEvent) {
        //考え中を出す
        event.deferReply(true).queue()

        //選ばれたオプション
        val options = event.selectedOptions

        //チャンネルを取得
        val targetChannel = event.guild!!.getGuildData().listTargetChannel ?: event.channel as GuildMessageChannel

        //言語のデータ
        val langData = event.guild!!.getLang()

        //オプションを取得
        val option = options[0] ?: return

        //':'で分裂させる
        val splitted = option.value.split(":")

        //タイマーの時
        if (option.value.startsWith("timer")) {
            //分を取得
            val minutes = splitted[2].toInt()

            //使用できる番号を取得
            val usableNumber = Timer.getUsableNumber(targetChannel)

            //埋まっているかを確認
            if (usableNumber.isEmpty()) {
                //最大数のメッセージを出力する
                event.hook.sendMessage(langData.timer.timerMaxWarning).queue()
                return
            }

            //使用できる番号の先頭を使用
            val number = usableNumber.first()

            //タイマーを開始
            Timer(targetChannel, number, minutes * 60).start()

            //メッセージを送信
            event.hook.sendMessage(
                number.format(
                    langData.component.select.runListEntry.langFormat(
                        "${splitted[1]}（${
                            langData.timer.minutes.langFormat(
                                minutes
                            )
                        }）"
                    )
                )
            ).queue()
            return
        }

        //ダイスの要素の時
        else if (option.value.startsWith("dice")) {
            //ダイスのコマンドを取得
            val command = splitted[2]

            //ダイスを実行
            Dice().roll(targetChannel, command, event.user)

            //メッセージを送信
            event.hook.sendMessage(langData.component.select.runListEntry.langFormat("${splitted[1]}（${command}）"))
                .queue()
        } else {
            //空白を送信して削除する
            event.hook.sendEmpty()
        }
    }

    override fun createSelectMenu(data: LinkedHashMap<String, String>, langData: LangData): SelectMenu {
        //メニューを作成
        val selectionMenu = StringSelectMenu.create(name)
        selectionMenu.placeholder = langData.component.select.placeholder
        selectionMenu.setRequiredRange(1, 1)

        //空白を追加
        selectionMenu.addOption("ㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤㅤ", "empty")

        //タイマの一覧を回す
        data.forEach {
            //名前を取得
            val name = it.key.split(":")[1]
            //タイマーの時
            if (it.key.startsWith("timer")) {
                //メニューに追加
                selectionMenu.addOption(
                    "$name (${langData.timer.minutes.langFormat(it.value)})",
                    "${it.key}:${it.value}",
                    Emoji.fromUnicode("⏱️")
                )

            }
            //ダイスの時
            else if (it.key.startsWith("dice")) {
                //メニューに追加
                selectionMenu.addOption(
                    "$name (${it.value})",
                    "${it.key}:${it.value}",
                    Emoji.fromUnicode("\uD83C\uDFB2")
                )
            }
        }

        //ビルドして返す
        return selectionMenu.build()
    }
}