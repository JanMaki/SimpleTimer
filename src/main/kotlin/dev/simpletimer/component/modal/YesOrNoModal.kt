package dev.simpletimer.component.modal

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

/**
 * YesのNoを問う確認のModal
 *
 */
object YesOrNoModal : ModalManager.Modal<YesOrNoModal.Data>("yes_or_no") {
    //ユーザーが開いているModalのデータを保管
    private val userDatum = mutableMapOf<Long, Data>()

    override fun run(event: ModalInteractionEvent) {
        //言語のデータを取得
        val langData = event.guild?.getLang() ?: return

        //データを取得
        val data = userDatum[event.user.idLong]
        //nullチェック
        if (data == null) {
            event.hook.sendMessage(langData.component.modal.missingValue).queue()
            return
        }

        //Inputの内容を確認
        when ((event.getValue("input")?.asString ?: "").uppercase().trim()) {
            "YES", "Y" -> {
                //Yesのときの処理
                data.yesAction.run(event)
            }

            "NO", "N" -> {
                //Noのときの処理
                data.noAction.run(event)
            }

            else -> {
                //エラーを送信
                event.hook.sendMessage(langData.component.modal.yesNoWarning).queue()
            }
        }
    }

    override fun createModal(data: Data, langData: LangData): Modal {
        val input = TextInput.create("input", langData.component.modal.check, TextInputStyle.SHORT)
            .setPlaceholder(langData.component.modal.yesNo)
            .setRequired(true)
            .build()
        //データを保管
        userDatum[data.userIdLong] = data
        //Modalを作って返す
        return Modal.create("yes_or_no", langData.component.modal.confirmation)
            .addActionRows(ActionRow.of(input))
            .build()
    }

    /**
     * YesOrNoのModalのデータ
     *
     * @property userIdLong ユーザーのIDの[Long]
     * @property yesAction Yesを選択した時に実行するアクション
     * @property noAction Noを選択した時に実行するアクション
     */
    data class Data(val userIdLong: Long, val yesAction: Action, val noAction: Action)

    /**
     * YesOrNoのModalの選択によって実行するアクションのインターフェース
     *
     */
    @FunctionalInterface
    fun interface Action {
        /**
         * 処理を実行する
         *
         * @param event 実行時に用いる[ModalInteractionEvent]
         */
        fun run(event: ModalInteractionEvent)
    }
}