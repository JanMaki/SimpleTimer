package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

object CommunityLinkButton : ButtonManager.Button<Any>("community_link") {
    override fun run(event: ButtonInteractionEvent) {
        //何もしない
    }

    override fun createButton(data: Any, langData: LangData): Button {
        return Button.link("https://discord.com/invite/qDw5TpXgzr", langData.component.button.joinCommunity)
    }
}