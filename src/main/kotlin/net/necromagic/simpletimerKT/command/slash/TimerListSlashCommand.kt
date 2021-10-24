package net.necromagic.simpletimerKT.command.slash

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.necromagic.simpletimerKT.TimerList
import net.necromagic.simpletimerKT.SimpleTimer
import net.necromagic.simpletimerKT.util.SendMessage

class TimerListSlashCommand {
    /**
     * リストを表示する
     */
    object List : SlashCommand("list", "タイマーリストを表示します") {
        init {
            setDefaultEnabled(true)
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //タイマーリストを送信する
            TimerList.sendList(event)
        }
    }

    /**
     * リストにタイマーを追加する
     *
     */
    object ListAdd : SlashCommand("list_add", "タイマーリストに追加をします") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.STRING, "名前", "タイマーの名前", true),
                OptionData(OptionType.INTEGER, "分", "時間を分単位で", true)
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val name = event.getOption("名前")!!.asString
            val seconds = event.getOption("分")!!.asLong.toInt()

            //コンフィグからタイマーの一覧を取得
            val config = SimpleTimer.instance.config
            val list = config.getTimerList(event.guild!!)

            //上限を確認
            if (list.size >= 10) {
                event.hook.sendMessage("*10個以上登録できません")
                return
            }

            //コンフィグでタイマーを追加して保存する
            config.addTimerList(event.guild!!, name, seconds)
            config.save()

            //メッセージを送信
            event.hook.sendMessage("タイマーを追加しました").queue()

            //タイマーリストを送信する
            TimerList.sendList(event)
        }
    }

    /**
     * リストからタイマーを削除する
     *
     */
    object ListRemove : SlashCommand("list_remove", "タイマーリストに追加をします") {
        init {
            setDefaultEnabled(true)

            addOptions(
                OptionData(OptionType.STRING, "名前", "タイマーの名前", true)
            )
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val name = event.getOption("名前")!!.asString

            //コンフィグからタイマーの一覧を取得し、有効なタイマーかを確認する
            val config = SimpleTimer.instance.config
            if (!config.getTimerList(event.guild!!).contains(name)) {
                //エラーのメッセージを送信
                event.hook.sendMessage("*無効なタイマーです").queue()
                return
            }

            //コンフィグでタイマーを削除して保存する
            config.removeTimerList(event.guild!!, name)
            config.save()

            //メッセージを送信
            event.hook.sendMessage("タイマーを削除しました").queue()

            //タイマーリストを送信する
            TimerList.sendList(event)
        }
    }

    object TimerChannel : SlashCommand("timer_channel", "一覧からタイマーを送信する") {
        init {
            setDefaultEnabled(true)

            addOption(OptionType.CHANNEL, "テキストチャンネル", "対象のチャンネル", true)
        }

        override fun run(command: String, event: SlashCommandEvent) {
            //オプションを取得
            val option = event.getOption("テキストチャンネル")

            //nullチェック
            if (option == null) {
                //エラーメッセージを送信
                replyCommandError(event)
                return
            }

            //チャンネルを取得
            val channel = option.asMessageChannel

            //nullチェック
            if (channel == null) {
                //エラーメッセージを送信
                replyCommandError(event)
                return
            }

            //チャンネルの権限を確認
            val permissions =
                event.guild!!.selfMember.getPermissions(event.guild!!.getTextChannelById(channel.idLong)!!)
            if (!permissions.contains(Permission.ADMINISTRATOR)) {
                if (!(
                            permissions.contains(Permission.VIEW_CHANNEL) &&
                                    permissions.contains(Permission.MESSAGE_SEND) &&
                                    permissions.contains(Permission.MESSAGE_TTS) &&
                                    permissions.contains(Permission.MESSAGE_EMBED_LINKS) &&
                                    permissions.contains(Permission.MESSAGE_HISTORY) &&
                                    permissions.contains(Permission.MESSAGE_EXT_EMOJI))
                ) {
                    //権限が不足しているメッセージを送信する
                    event.hook.sendMessageEmbeds(SendMessage.errorEmbed).complete()
                    return
                }
            }

            //コンフィグに設定をし、保存
            val config = SimpleTimer.instance.config
            config.setTimerChannel(event.guild!!, channel)
            config.save()

            //メッセージを送信
            event.hook.sendMessage("一覧からタイマーを実行するチャンネルを**${channel.name}**に変更しました").queue()
        }
    }
}