package dev.simpletimer.component.modal

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.database.transaction.TimerDataTransaction
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

/**
 * デバッグ機能のModal
 *
 */
object DebugModal : ModalManager.Modal<Byte>("debug") {
    override fun run(event: ModalInteractionEvent) {
        //入力した値を取得
        val inputValue = event.getValue("input")?.asString ?: return

        //値を確認
        when (inputValue) {
            //タイマーの稼働数を確認する
            "count" -> {
                //タイマーの稼働数を送信する
                event.hook.sendMessage("${TimerDataTransaction.count()}個のタイマーが稼働しています").queue()
            }
            //ギルドにコマンドを強制的に追加させる
            "commands" -> {
                //すべてのコマンド
                SlashCommandManager.slashCommands.forEach {
                    //コマンドを登録
                    event.guild?.upsertCommand(it)?.queue()
                }

                //メッセージを送信
                event.hook.sendMessage("コマンドを更新しました").queue()
            }
            //接続しているVCの数を確認する
            "audio" -> {
                //接続しているVCの数を送信する
                event.hook.sendMessage(
                    "${
                        SimpleTimer.instance.audioManager.getAudioPlayers().filter { it.isConnected() }.size
                    }個のVCに接続しています"
                ).queue()
            }
            //無効な値の時
            else -> {
                //メッセージを送信
                event.hook.sendMessage("*無効な入力です").queue()
            }
        }
    }

    //Modalを作成する
    private val modal = Modal.create(name, "Debug").addActionRow(
        TextInput.create("input", "入力", TextInputStyle.SHORT)
            .setPlaceholder("値を入力してください")
            .setRequired(true)
            .build()
    ).build()

    override fun createModal(data: Byte, langData: LangData): Modal {
        //Modalを返す
        return modal
    }
}