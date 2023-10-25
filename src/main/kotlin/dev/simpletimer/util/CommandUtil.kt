package dev.simpletimer.util

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

/**
 * コマンドで使用するUtil
 *
 */
object CommandUtil {
    /**
     * コマンドエラーを送信する
     *
     * @param event [SlashCommandInteractionEvent] スラッシュコマンドのイベント
     */
    fun replyCommandError(event: SlashCommandInteractionEvent) {
        val error = event.guild?.getLang()?.command?.error ?: return
        event.hook.sendMessage(error).queue({}) {
            event.reply(error).queue()
        }
    }

    /**
     * 言語のパスから[OptionData]を作成する
     *
     * @param type [OptionType] オプションのタイプ
     * @param langPath [CommandInfoPath] コマンドの言語情報までのパス
     * @return 作成した[OptionData]
     */
    fun createOptionData(
        type: OptionType,
        langPath: CommandInfoPath,
        required: Boolean = false,
        autoComplete: Boolean = false
    ): OptionData {
        //データコンテナを取得
        val dataContainer = SimpleTimer.instance.dataContainer

        //日本語の言語のデータを取得
        val japaneseLangData =
            dataContainer.getCommandInfoLangData(Lang.JPA, langPath) ?: throw IllegalArgumentException()

        //オプションを作成して返す
        return OptionData(
            type,
            japaneseLangData.name,
            japaneseLangData.description,
            required,
            autoComplete
        ).apply {
            //日本語以外の言語を回す
            Lang.entries.filter { it != Lang.JPA }.forEach {
                //言語のデータを取得
                val langData = dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                //ローカライズを設定
                setNameLocalization(it.discordLocal, langData.name)
                setDescriptionLocalization(it.discordLocal, langData.description)
            }
        }
    }

    /**
     * 言語のパスから[SubcommandData]を作成する
     *
     * @param langPath [CommandInfoPath] コマンドの言語情報までのパス
     * @return 作成した[SubcommandData]
     */
    fun createSubCommandData(langPath: CommandInfoPath): SubcommandData {
        //データコンテナを取得
        val dataContainer = SimpleTimer.instance.dataContainer

        //日本語の言語のデータを取得
        val japaneseLangData =
            dataContainer.getCommandInfoLangData(Lang.JPA, langPath) ?: throw IllegalArgumentException()

        //サブコマンドを作成して返す
        return SubcommandData(japaneseLangData.name, japaneseLangData.description).apply {
            //日本語以外の言語を回す
            Lang.entries.filter { it != Lang.JPA }.forEach {
                //言語のデータを取得
                val langData = dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                //ローカライズを設定
                setNameLocalization(it.discordLocal, langData.name)
                setDescriptionLocalization(it.discordLocal, langData.description)
            }
        }
    }

    fun createChoice(langPath: CommandInfoPath, value: Long): Command.Choice {
        //データコンテナを取得
        val dataContainer = SimpleTimer.instance.dataContainer

        //日本語の言語のデータを取得
        val japaneseLangData =
            dataContainer.getCommandInfoLangData(Lang.JPA, langPath) ?: throw IllegalArgumentException()

        //チョイスを作成して返す
        return Command.Choice(
            japaneseLangData.name + if (japaneseLangData.description.isNotEmpty()) ": " + japaneseLangData.description else "",
            value
        ).apply {
            //日本語以外の言語を回す
            Lang.entries.filter { it != Lang.JPA }.forEach {
                //言語のデータを取得
                val langData = dataContainer.getCommandInfoLangData(it, langPath) ?: return@forEach
                //ローカライズを設定
                setNameLocalization(
                    it.discordLocal,
                    langData.name + if (langData.description.isNotEmpty()) ": " + langData.description else ""
                )
            }
        }
    }
}