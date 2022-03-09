package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.data.getGuildData
import dev.simpletimer.list.ListMenu
import dev.simpletimer.util.SendMessage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class TimerListSlashCommand {
    /**
     * 一覧を表示する
     */
    object List : SlashCommand("list", "一覧を表示します") {
        init {
            isDefaultEnabled = true
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //一覧を送信する
            ListMenu.sendList(event)
        }
    }

    /**
     * 一覧に要素を追加する
     *
     */
    object ListAdd : SlashCommand("list_add", "一覧に要素を追加をします") {
        init {
            isDefaultEnabled = true

            addSubcommands(
                SubcommandData("timer", "タイマーを一覧に追加する").addOptions(
                    OptionData(OptionType.STRING, "名前", "タイマーの名前", true),
                    OptionData(OptionType.INTEGER, "分", "時間を分単位で", true)
                ),
                SubcommandData("dice", "ダイスを一覧に追加する").addOptions(
                    OptionData(OptionType.STRING, "名前", "ダイスの名前", true),
                    OptionData(OptionType.STRING, "ダイス", "ダイスの内容", true)
                )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドのデータを取得
            val guildData = event.guild?.getGuildData() ?: return

            //同期の確認
            if (guildData.listSync) {
                event.hook.sendMessage("このサーバーでは一覧を同期しています。").queue()
                return
            }

            //オプションを取得
            val name = event.getOption("名前")!!.asString

            //文字数制限
            if (name.length >= 10) {
                event.hook.sendMessage("*名前の文字数は10文字以下にしてください").queue()
                return
            }

            //:を挟まれないようにする
            if (name.contains(":")) {
                event.hook.sendMessage("*名前に使用できない文字が含まれています").queue()
                return
            }

            //ギルドのデータから一覧を取得
            val list = guildData.list

            //上限を確認
            if (list.size >= 10) {
                event.hook.sendMessage("*10個以上登録できません").queue()
                return
            }

            //サブコマンドを確認
            when (event.subcommandName) {
                "timer" -> {
                    //分を取得
                    val seconds = event.getOption("分")!!.asLong.toInt()

                    //ギルドのデータでタイマーを追加
                    guildData.list["timer:${name}"] = seconds.toString()
                }
                "dice" -> {
                    //ダイスの内容を取得
                    val rollCommand = event.getOption("ダイス")!!.asString

                    //ギルドのデータでダイスを追加
                    guildData.list["dice:${name}"] = rollCommand
                }
            }

            //保存
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを送信
            event.hook.sendMessage("一覧に追加しました").queue()

            //タイマー一覧を送信する
            ListMenu.sendList(event)
        }
    }

    /**
     * 一覧から要素を削除する
     *
     */
    object ListRemove : SlashCommand("list_remove", "一覧から要素を削除をします") {
        init {
            isDefaultEnabled = true

            addOptions(
                OptionData(OptionType.STRING, "名前", "要素の名前", true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドのデータを取得
            val guildData = event.guild?.getGuildData() ?: return

            //オプションを取得
            val name = event.getOption("名前")!!.asString

            //同期の確認
            if (guildData.listSync) {
                event.hook.sendMessage("このサーバーでは一覧を同期しています。").queue()
                return
            }

            //ギルドのデータから一覧を取得し、有効な要素かを確認する
            if (!guildData.list.contains(name)) {
                //エラーのメッセージを送信
                event.hook.sendMessage("*無効な要素です").queue()
                return
            }

            //ギルドのデータを削除して保存する
            guildData.list.remove(name)
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを送信
            event.hook.sendMessage("要素を削除しました").queue()

            //一覧を送信する
            ListMenu.sendList(event)
        }
    }

    object ListTargetChannel : SlashCommand("list_target", "タイマーやダイスを送信するチャンネルを設定する") {
        init {
            isDefaultEnabled = true

            addOption(OptionType.CHANNEL, "テキストチャンネル", "対象のチャンネル", true)
        }

        override fun run(event: SlashCommandInteractionEvent) {
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
            val permissions = event.guild!!.selfMember.getPermissions(channel)
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

            //ギルドのデータに設定をし、保存
            event.guild!!.getGuildData().listTargetChannel = channel
            SimpleTimer.instance.dataContainer.saveGuildsData()

            //メッセージを送信
            event.hook.sendMessage("一覧からタイマーやダイスを実行するチャンネルを**${channel.name}**に変更しました").queue({}, {})
        }
    }

    object SyncList : SlashCommand("list_sync", "一覧を他のサーバーと同期します") {
        init {
            isDefaultEnabled = true

            addSubcommands(
                SubcommandData("enable", "同期を行うようにする")
                    .addOption(OptionType.STRING, "id", "同期する対象のサーバーで出力されたIDを入れてください", true),
                SubcommandData("disable", "同期を行わないようにする")
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
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


            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            //メンションの方式
            guildData.listSync = bool

            if (bool) {
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

                if (guild.idLong != long) {
                    val targetGuild = SimpleTimer.instance.getGuild(long)

                    if (targetGuild == null) {
                        event.hook.sendMessage("*無効なIDです").queue()
                        return
                    } else {
                        event.hook.sendMessage("同期を開始しました").queue()
                        guildData.syncTarget = targetGuild
                    }
                } else {
                    event.hook.sendMessage("*対象のサーバーが同じサーバーです").queue()
                    return
                }

            } else {
                event.hook.sendMessage("同期を終了しました").queue()
            }

            SimpleTimer.instance.dataContainer.saveGuildsData()
        }
    }

    object GetID : SlashCommand("list_id", "同期に必要なIDを取得します") {
        init {
            isDefaultEnabled = true
        }

        override fun run(event: SlashCommandInteractionEvent) {
            val id = event.guild!!.idLong.toString(36)
            event.hook.sendMessage("IDは`${id}`です。\n他のサーバーで`/list_sync enable id: ${id}`を行うことで、このサーバーの一覧を同期できます").queue()
        }
    }
}