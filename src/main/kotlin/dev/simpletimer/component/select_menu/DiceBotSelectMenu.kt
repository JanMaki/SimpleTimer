package dev.simpletimer.component.select_menu

import dev.simpletimer.bcdice_kt.bcdice_task.GameSystem
import dev.simpletimer.dice.bcdice.BCDiceManager
import dev.simpletimer.extension.emoji
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu

/**
 * DiceBotを選択するセレクトメニュー
 *
 */
object DiceBotSelectMenu : SelectMenuManager.SelectMenu<ArrayList<GameSystem>>("dice_bot") {
    override fun run(event: SelectMenuInteractionEvent) {
        //BCDiceManagerに処理を任せる
        BCDiceManager.instance.selectFromSelectMenuInteraction(event)
    }

    override fun createSelectMenu(data: ArrayList<GameSystem>): SelectMenu {
        //メニューを作成
        val selectionMenu = SelectMenu.create(name)
        selectionMenu.placeholder = "選択"
        selectionMenu.setRequiredRange(1, 1)
        //GameManagerを選択肢に
        data.withIndex().forEach {
            selectionMenu.addOption(it.value.name, it.value.id, Emoji.fromUnicode((it.index + 1).emoji))
        }
        //作成して返す
        return selectionMenu.build()
    }
}