package dev.simpletimer.extension

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping

//CommandInteractionPayloadを拡張している

/**
 * 言語のデータからコマンドのオプションを取得する
 *
 * @param langPath [CommandInfoPath] コマンドの言語の情報
 * @return [OptionMapping]?
 */
fun CommandInteractionPayload.getOption(langPath: CommandInfoPath): OptionMapping? {
    //日本語の言語のデータを取得
    val japaneseLangData =
        SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JAP, langPath)
            ?: throw IllegalArgumentException()

    //オプションを取得して返す
    return getOption(japaneseLangData.name)
}
