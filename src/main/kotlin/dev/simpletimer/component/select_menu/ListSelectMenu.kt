package dev.simpletimer.component.select_menu

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.dice.Dice
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.langFormat
import dev.simpletimer.extension.sendEmpty
import dev.simpletimer.timer.Timer
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
                    event.hook.sendMessage(number.format(  langData.component.select.runListEntry.langFormat("${splitted[1]}（${langData.timer.minutes.langFormat(minutes)}）"))).queue()
                    return
                }
            }

            //最大数のメッセージを出力する
            event.hook.sendMessage(langData.timer.timerMaxWarning).queue()
        }
        //ダイスの要素の時
        else if (option.value.startsWith("dice")) {
            //ダイスのコマンドを取得
            val command = splitted[2]

            //ダイスを実行
            Dice().roll(channel, command, event.user)

            //メッセージを送信
            event.hook.sendMessage(langData.component.select.runListEntry.langFormat("${splitted[1]}（${command}）")).queue()
        } else {
            //空白を送信して削除する
            event.hook.sendEmpty()
        }
    }

    override fun createSelectMenu(data: LinkedHashMap<String, String>, langData: LangData): SelectMenu {
        //メニューを作成
        val selectionMenu = SelectMenu.create(name)
        selectionMenu.placeholder = langData.component.select.placeholder
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
                selectionMenu.addOption("$name (${langData.timer.minutes.langFormat(it.value)})", "${it.key}:${it.value}", Emoji.fromUnicode("⏱️"))

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