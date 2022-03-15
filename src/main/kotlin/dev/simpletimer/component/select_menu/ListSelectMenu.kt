package dev.simpletimer.component.select_menu

import dev.simpletimer.dice.Dice
import dev.simpletimer.timer.Timer
import dev.simpletimer.util.getGuildData
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import java.util.*

/**
 * 一覧の選択メニュー
 *
 */
object ListSelectMenu : SelectMenuManager.SelectMenu<LinkedHashMap<String, String>>("list") {
    override fun run(event: SelectMenuInteractionEvent) {
        //選ばれたオプション
        val options = event.selectedOptions

        //チャンネルを取得
        val channel = event.guild!!.getGuildData().listTargetChannel ?: event.channel as GuildMessageChannel

        //オプションを取得
        val option = options[0] ?: return

        //':'で分裂させる
        val splitted = option.value.split(":")

        //タイマーの時
        if (option.value.startsWith("timer")) {
            //分を取得
            val minutes = splitted[2].toInt()

            //チャンネルのタイマーを取得する
            val channelTimers = Timer.channelsTimersMap.getOrPut(channel) { EnumMap(Timer.Number::class.java) }

            //番号を確認
            for (number in Timer.Number.values()) {
                //その番号のタイマーが動いているかを確認
                if (!channelTimers.containsKey(number)) {
                    //タイマーを開始してインスタンスを代入する
                    channelTimers[number] = Timer(channel, number, minutes * 60, event.guild!!)
                    Timer.channelsTimersMap[channel] = channelTimers

                    //メッセージを送信
                    event.hook.sendMessage(number.format("${splitted[1]}（${minutes}分）を実行しました")).queue({}, {})
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(":x: これ以上タイマーを動かすことはできません（最大: 4）").queue({}, {})
        }
        //ダイスの要素の時
        else if (option.value.startsWith("dice")) {
            //ダイスのコマンドを取得
            val command = splitted[2]

            //ダイスを実行
            Dice().roll(channel, command, event.user)

            //メッセージを送信
            event.hook.sendMessage("${splitted[1]}（${command}）を実行しました").queue({}, {})
        } else {
            //空白を送信して削除する
            event.hook.sendMessage("|| ||").queue{
                it.delete().queue({}, {})
            }
        }
    }

    override fun createSelectMenu(data: LinkedHashMap<String, String>): SelectMenu {
        //メニューを作成
        val selectionMenu = SelectMenu.create(name)
        selectionMenu.placeholder = "選択"
        selectionMenu.setRequiredRange(1, 1)

        //空白を追加
        selectionMenu.addOption("　", "empty")

        //タイマの一覧を回す
        data.forEach {
            //名前を取得
            val name = it.key.split(":")[1]
            //タイマーの時
            if (it.key.startsWith("timer")) {
                //メニューに追加
                selectionMenu.addOption("$name (${it.value}分)", "${it.key}:${it.value}", Emoji.fromUnicode("⏱️"))

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