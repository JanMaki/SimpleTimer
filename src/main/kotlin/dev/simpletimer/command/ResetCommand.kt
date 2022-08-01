package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.modal.YesOrNoModal
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import dev.simpletimer.extension.sendEmpty
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

/**
 * データをリセットする
 *
 */
object ResetCommand :
    SlashCommandManager.SlashCommand(CommandInfoPath.RESET, deferReply = false) {
    override fun run(event: SlashCommandInteractionEvent) {
        val guild = event.guild ?: return

        //確認のModalでYesを選択したときの処理
        val yesAction = YesOrNoModal.Action {
            SimpleTimer.instance.dataContainer.resetGuildData(guild)

            it.hook.sendMessage(guild.getLang().command.reset.reset).queue()
        }

        //確認のModalでNoを選択したときの処理
        val noAction = YesOrNoModal.Action {
            it.hook.sendEmpty()
        }

        //Modalを作成して送信
        event.replyModal(
            YesOrNoModal.createModal(
                YesOrNoModal.Data(
                    event.user.idLong,
                    yesAction,
                    noAction
                ), guild.getLang()
            )
        ).queue()
    }
}