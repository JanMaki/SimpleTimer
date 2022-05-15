package dev.simpletimer.command

import dev.simpletimer.SimpleTimer
import dev.simpletimer.component.modal.YesOrNoModal
import dev.simpletimer.extension.*
import dev.simpletimer.list.ListMenu
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class ListCommands {
    /**
     * 一覧を表示する
     */
    object List : SlashCommandManager.SlashCommand("list", "一覧を表示します") {
        override fun run(event: SlashCommandInteractionEvent) {
            //一覧を送信する
            ListMenu.sendList(event)
        }
    }

    /**
     * 一覧に要素を追加・上書きをする
     *
     */
    object ListAdd : SlashCommandManager.SlashCommand("list_add", "一覧に要素を追加・上書きをします") {
        init {
            addSubcommands(
                SubcommandData("timer", "タイマーを一覧に追加・上書きをする").addOptions(
                    OptionData(OptionType.STRING, "名前", "タイマーの名前", true),
                    OptionData(OptionType.INTEGER, "分", "時間を分単位で", true)
                ),
                SubcommandData("dice", "ダイスを一覧に追加・上書きをする").addOptions(
                    OptionData(OptionType.STRING, "名前", "ダイスの名前", true),
                    OptionData(OptionType.STRING, "ダイス", "ダイスの内容", true)
                )
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            //言語のデータ
            val langData = guild.getLang()

            //同期の確認
            if (guildData.listSync) {
                event.hook.sendMessage(langData.command.list.synced).queue()
                return
            }

            //オプションを取得
            val name = event.getOption("名前")!!.asString

            //文字数制限
            if (name.length >= 10) {
                event.hook.sendMessage(langData.command.list.longLengthWarning).queue()
                return
            }

            //:を挟まれないようにする
            if (name.contains(":")) {
                event.hook.sendMessage(langData.command.list.unusableCharacter).queue()
                return
            }

            //ギルドのデータから一覧を取得
            val list = guildData.list

            //上限を確認
            if (list.size >= 10) {
                event.hook.sendMessage(langData.command.list.maxEntry).queue()
                return
            }

            //サブコマンドを確認
            when (event.subcommandName) {
                "timer" -> {
                    //ギルドのデータでタイマーを追加
                    guildData.list["timer:${name}"] = event.getOption("分")!!.asInt.toString()
                }
                "dice" -> {
                    //ギルドのデータでダイスを追加
                    guildData.list["dice:${name}"] = event.getOption("ダイス")!!.asString
                }
            }

            //保存
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを送信
            event.hook.sendMessage(langData.command.list.add).queue()

            //タイマー一覧を送信する
            ListMenu.sendList(event)
        }
    }

    /**
     * 一覧から要素を削除する
     *
     */
    object ListRemove : SlashCommandManager.SlashCommand("list_remove", "一覧から要素を削除をします") {
        init {
            addOptions(
                OptionData(OptionType.STRING, "名前", "要素の名前", true, true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            //言語のデータ
            val langData = guild.getLang()

            //オプションを取得
            val name = event.getOption("名前")!!.asString

            //同期の確認
            if (guildData.listSync) {
                event.hook.sendMessage(langData.command.list.synced).queue()
                return
            }

            //ギルドのデータから一覧を取得し、有効な要素かを確認する
            if (!guildData.list.contains("dice:${name}") && !guildData.list.contains("timer:${name}")) {
                //エラーのメッセージを送信
                event.hook.sendMessage(langData.command.list.missingEntryWarning).queue()
                return
            }

            //ギルドのデータを削除して保存する
            guildData.list.remove("dice:${name}")
            guildData.list.remove("timer:${name}")
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを送信
            event.hook.sendMessage(langData.command.list.remove).queue()

            //一覧を送信する
            ListMenu.sendList(event)
        }

        override fun autoComplete(event: CommandAutoCompleteInteractionEvent) {
            //オプション名を確認
            if (event.focusedOption.name != "名前") return

            //ギルドのデータを取得
            val guildData = event.guild?.getGuildData() ?: return

            //文字列のコレクションを返す
            event.replyChoiceStrings(guildData.list.keys.map { it.split(":")[1] }.filter {
                //リストの要素を確認
                it.startsWith(event.focusedOption.value)
            }).queue()
        }
    }

    /**
     * 一覧の全削除
     *
     */
    object ListClear : SlashCommandManager.SlashCommand("list_clear", "一覧の要素をすべて削除します", deferReply = false) {
        override fun run(event: SlashCommandInteractionEvent) {
            //ギルドを取得
            val guild = event.guild ?: return
            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            //言語のデータ
            val langData = guild.getLang()

            //同期の確認
            if (guildData.listSync) {
                event.hook.sendMessage(langData.command.list.synced).queue()
                return
            }

            //確認のModalでYesを選択したときの処理
            val yesAction = YesOrNoModal.Action {
                //一覧を削除
                guildData.list.clear()
                //保存
                SimpleTimer.instance.dataContainer.saveGuildsData(guild)
                //メッセージを送信
                it.hook.sendMessage(langData.command.list.clear).queue()
            }
            //確認のModalでNoを選択したときの処理
            val noAction = YesOrNoModal.Action {
                //空白を送信
                it.hook.sendEmpty()
            }

            //Modalを作成して返す
            event.replyModal(
                YesOrNoModal.createModal(
                    YesOrNoModal.Data(event.user.idLong, yesAction, noAction),
                    guild.getLang()
                )
            )
                .queue()
        }
    }

    /**
     * 一覧の送信の対象チャンネルを変更する
     *
     */
    object ListTargetChannel : SlashCommandManager.SlashCommand("list_target", "タイマーやダイスを送信するチャンネルを設定する") {
        init {
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

            //管理者権限か、必要な権限を確認
            if (!event.guildChannel.checkSimpleTimerPermission()) {
                //権限が不足しているメッセージを送信する
                event.hook.sendMessageEmbeds(SimpleTimer.instance.getErrorEmbed(event.guildChannel)).queue()
                return
            }

            //ギルドのデータに設定をし、保存
            val guild = event.guild!!
            guild.getGuildData().listTargetChannel = channel
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを送信
            event.hook.sendMessage(guild.getLang().command.list.changeChannel.langFormat("**${channel.name}**")).queue()
        }
    }

    /**
     * 一覧を他のサーバーと同期する
     *
     */
    object SyncList : SlashCommandManager.SlashCommand("list_sync", "一覧を他のサーバーと同期します") {
        init {
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

            //言語のデーター
            val langData = guild.getLang()

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

                //36進数にからLongにする
                val long = java.lang.Long.parseLong(id, 36)

                //ギルドのIDが違うかを確認
                if (guild.idLong != long) {
                    //ターゲットのギルドを取得
                    val targetGuild = SimpleTimer.instance.getGuild(long)

                    //nullチェック
                    if (targetGuild == null) {
                        //メッセージを送信
                        event.hook.sendMessage(langData.command.list.invalidID).queue()
                        return
                    } else {
                        //メッセージを送信
                        event.hook.sendMessage(langData.command.list.startSync).queue()
                        //ターゲットのギルドを設定
                        guildData.syncTarget = targetGuild
                    }
                } else {
                    event.hook.sendMessage(langData.command.list.targetSame).queue()
                    return
                }

            } else {
                event.hook.sendMessage(langData.command.list.finishSync).queue()
            }

            //ギルドのデータを保存
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)
        }
    }


    /**
     * 一覧を他のサーバーと同期する
     *
     */
    object CopyList : SlashCommandManager.SlashCommand("list_copy", "他のサーバーの一覧をコピーします") {
        init {
            addOption(OptionType.STRING, "id", "同期する対象のサーバーで出力されたIDを入れてください", true)
        }

        override fun run(event: SlashCommandInteractionEvent) {
            val guild = event.guild!!

            //ギルドのデータを取得
            val guildData = guild.getGuildData()

            //言語のデータ
            val langData = guild.getLang()

            //オプションを取得
            val option = event.getOption("id")

            //nullチェック
            if (option == null) {
                replyCommandError(event)
                return
            }

            //Stringに変換
            val id = option.asString

            //36進数にからLongにする
            val long = java.lang.Long.parseLong(id, 36)

            //ギルドのIDが違うかを確認
            if (guild.idLong == long) {
                event.hook.sendMessage(langData.command.list.targetSame).queue()
                return
            }

            //ターゲットのギルドを取得
            val targetGuild = SimpleTimer.instance.getGuild(long)

            //nullチェック
            if (targetGuild == null) {
                //メッセージを送信
                event.hook.sendMessage(langData.command.list.invalidID).queue()
                return
            }

            //内容をコピーする
            guildData.list = LinkedHashMap<String, String>().apply {
                this.putAll(targetGuild.getGuildData().list)
            }
            //ギルドのデータを保存
            SimpleTimer.instance.dataContainer.saveGuildsData(guild)

            //メッセージを送信
            event.hook.sendMessage(langData.command.list.copy).queue()
        }
    }

    /**
     * ギルドのIDを取得
     *
     */
    object GetID : SlashCommandManager.SlashCommand("list_id", "同期に必要なIDを取得します") {
        override fun run(event: SlashCommandInteractionEvent) {
            //メッセージを送信
            event.hook.sendMessage(event.guild!!.getLang().command.list.id.langFormat(event.guild!!.idLong.toString(36)))
                .queue()
        }
    }
}