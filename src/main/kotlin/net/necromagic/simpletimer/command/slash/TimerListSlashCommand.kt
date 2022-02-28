package net.necromagic.simpletimer.command.slash

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.necromagic.simpletimer.TimerList
import net.necromagic.simpletimer.SimpleTimer
import net.necromagic.simpletimer.util.SendMessage

class TimerListSlashCommand {
    /**
     * リストを表示する
     */
    object List : SlashCommand("list", "タイマーリストを表示します") {
        init {
            setDefaultEnabled(true)
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //コンフィグを取得
            val config = SimpleTimer.instance.config

            //同期の確認
            if(config.getListSync(event.guild!!)){
                event.hook.sendMessage("このサーバーでは一覧を同期しています。").queue()
                return
            }

            //オプションを取得
            val name = event.getOption("名前")!!.asString
            val seconds = event.getOption("分")!!.asLong.toInt()

            //コンフィグから一覧を取得
            val list = config.getTimerList(event.guild!!)

            //上限を確認
            if (list.size >= 10) {
                event.hook.sendMessage("*10個以上登録できません").queue()
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

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //コンフィグを取得
            val config = SimpleTimer.instance.config

            //オプションを取得
            val name = event.getOption("名前")!!.asString

            //同期の確認
            if(config.getListSync(event.guild!!)){
                event.hook.sendMessage("このサーバーでは一覧を同期しています。").queue()
                return
            }

            //コンフィグからタイマーの一覧を取得し、有効なタイマーかを確認する
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

        override fun run(command: String, event: SlashCommandInteractionEvent) {
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
                event.guild!!.selfMember.getPermissions(
                    event.guild!!.getTextChannelById(channel.idLong) ?:
                    event.guild!!.getThreadChannelById(channel.idLong)!!
                )
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
                    event.hook.sendMessageEmbeds(SendMessage.errorEmbed).queue({}, {})
                    return
                }
            }

            //コンフィグに設定をし、保存
            val config = SimpleTimer.instance.config
            config.setTimerChannel(event.guild!!, channel)
            config.save()

            //メッセージを送信
            event.hook.sendMessage("一覧からタイマーを実行するチャンネルを**${channel.name}**に変更しました").queue({}, {})
        }
    }

    object SyncList : SlashCommand("list_sync", "一覧を他のサーバーと同期します"){
        init {
            setDefaultEnabled(true)

            addSubcommands(
                SubcommandData("enable", "同期を行うようにする")
                    .addOption(OptionType.STRING, "id","同期する対象のサーバーで出力されたIDを入れてください", true),
                SubcommandData("disable", "同期を行わないようにする")
            )
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            //サブコマンドを取得
            val subCommand = event.subcommandName

            //nullチェック
            if (subCommand == null) {
                replyCommandError(event)
                return
            }

            //bool値に変換
            val bool = when (subCommand) {
                "enable" -> {
                    true
                }
                "disable" -> {
                    false
                }
                else -> {
                    return
                }
            }

            //コンフィグへ設定
            val config = SimpleTimer.instance.config
            val guild = event.guild!!

            //メンションの方式
            config.setListSync(guild, bool)



            if (bool){
                //オプションを取得
                val option = event.getOption("id")

                //nullチェック
                if (option == null) {
                    replyCommandError(event)
                    return
                }

                //Stringに変換
                val id = option.asString

                val long = java.lang.Long.parseLong(id, 36)

                if (guild.idLong != long){
                    val targetGuild = SimpleTimer.instance.getGuild(long)

                    if (targetGuild == null){
                        event.hook.sendMessage("*無効なIDです").queue()
                        return
                    }else {
                        event.hook.sendMessage("同期を開始しました").queue()
                        config.setSyncTarget(guild, targetGuild)
                    }
                }else {
                    event.hook.sendMessage("*対象のサーバーが同じサーバーです").queue()
                    return
                }

            }else {
                event.hook.sendMessage("同期を終了しました").queue()
            }

            config.save()
        }
    }

    object GetID : SlashCommand("list_id", "同期に必要なIDを取得します"){
        init {
            setDefaultEnabled(true)
        }

        override fun run(command: String, event: SlashCommandInteractionEvent) {
            val id = event.guild!!.idLong.toString(36)
            event.hook.sendMessage("IDは`${id}`です。\n他のサーバーで`/list_sync enable id: ${id}`を行うことで、このサーバーの一覧を同期できます").queue()
        }
    }
}