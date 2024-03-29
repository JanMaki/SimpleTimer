package dev.simpletimer.command.list

import dev.simpletimer.SimpleTimer
import dev.simpletimer.command.SlashCommandManager
import dev.simpletimer.component.modal.YesOrNoModal
import dev.simpletimer.data.lang.Lang
import dev.simpletimer.data.lang.lang_data.command_info.CommandInfoPath
import dev.simpletimer.extension.*
import dev.simpletimer.list.ListMenu
import dev.simpletimer.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

/**
 * 一覧を表示する
 *
 */
object ListShow : SlashCommandManager.SubCommand(CommandInfoPath.LIST_SHOW) {
    override fun run(event: SlashCommandInteractionEvent) {
        //一覧を送信する
        ListMenu.sendList(event)
    }
}

object ListAddSubCommands {
    object ListAddTimer : SlashCommandManager.SubCommand(CommandInfoPath.LIST_ADD_TIMER) {
        init {
            addOptions(
                CommandUtil.createOptionData(OptionType.STRING, CommandInfoPath.LIST_OPT_TIMER_NAME, true),
                CommandUtil.createOptionData(OptionType.INTEGER, CommandInfoPath.MINUTES, true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            ListAddSubCommands.run(event)
        }
    }

    object ListAddDice : SlashCommandManager.SubCommand(CommandInfoPath.LIST_ADD_DICE) {
        init {
            addOptions(
                CommandUtil.createOptionData(OptionType.STRING, CommandInfoPath.LIST_OPT_DICE_NAME, true),
                CommandUtil.createOptionData(OptionType.STRING, CommandInfoPath.LIST_OPT_DICE, true)
            )
        }

        override fun run(event: SlashCommandInteractionEvent) {
            ListAddSubCommands.run(event)
        }
    }


    fun run(event: SlashCommandInteractionEvent) {
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

        //各サブコマンドの名前
        val timerSubcommandName = SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JPA, CommandInfoPath.LIST_ADD_TIMER)?.name
        val diceSubcommandName = SimpleTimer.instance.dataContainer.getCommandInfoLangData(Lang.JPA, CommandInfoPath.LIST_ADD_DICE)?.name

        //オプションを取得
        val name = when (event.subcommandName) {
            timerSubcommandName -> {
                //タイマーのときのオプションを取得
                event.getOption(CommandInfoPath.LIST_OPT_TIMER_NAME)!!.asString
            }

            diceSubcommandName -> {
                //ダイスのときのオプションを取得
                event.getOption(CommandInfoPath.LIST_OPT_DICE_NAME)!!.asString
            }

            else -> {
                CommandUtil.replyCommandError(event)
                return
            }
        }

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
            timerSubcommandName -> {
                //ギルドのデータでタイマーを追加
                guildData.list["timer:${name}"] = event.getOption(CommandInfoPath.MINUTES)!!.asInt.toString()
            }

            diceSubcommandName -> {
                //ギルドのデータでダイスを追加
                guildData.list["dice:${name}"] = event.getOption(CommandInfoPath.LIST_OPT_DICE)!!.asString
            }

            else -> {
                CommandUtil.replyCommandError(event)
                return
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
object ListRemove : SlashCommandManager.SubCommand(CommandInfoPath.LIST_REMOVE) {
    init {
        addOptions(
            CommandUtil.createOptionData(
                OptionType.STRING,
                CommandInfoPath.LIST_OPT_ELEMENT_NAME,
                required = true,
                autoComplete = true
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

        //オプションを取得
        val name = event.getOption(CommandInfoPath.LIST_OPT_ELEMENT_NAME)!!.asString

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
object ListClear : SlashCommandManager.SubCommand(CommandInfoPath.LIST_CLEAR, deferReply = false) {
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
object ListTargetChannel : SlashCommandManager.SubCommand(CommandInfoPath.LIST_TARGET_CHANNEL) {
    init {
        addOptions(CommandUtil.createOptionData(OptionType.CHANNEL, CommandInfoPath.LIST_OPT_CHANNEL, true))
    }

    override fun run(event: SlashCommandInteractionEvent) {
        //オプションを取得
        val option = event.getOption(CommandInfoPath.LIST_OPT_CHANNEL)

        //nullチェック
        if (option == null) {
            //エラーメッセージを送信
            CommandUtil.replyCommandError(event)
            return
        }

        val guild = event.guild!!

        //チャンネルを取得
        val channel = try {
            option.asChannel.asGuildMessageChannel()
        } catch (e: IllegalStateException) {
            //エラーのメッセージを送信
            event.hook.sendMessage(guild.getLang().command.list.targetError).queue()
            return
        }


        //管理者権限か、必要な権限を確認
        if (!event.guildChannel.checkSimpleTimerPermission()) {
            //権限が不足しているメッセージを送信する
            event.hook.sendMessageEmbeds(SimpleTimer.instance.getErrorEmbed(event.guildChannel)).queue()
            return
        }

        //ギルドのデータに設定をし、保存
        guild.getGuildData().listTargetChannel = channel
        SimpleTimer.instance.dataContainer.saveGuildsData(guild)

        //メッセージを送信
        event.hook.sendMessage(guild.getLang().command.list.changeChannel.langFormat("**${channel.name}**")).queue()
    }
}


/**
 * 同期を有効にする
 *
 */
object ListSync : SlashCommandManager.SubCommand(CommandInfoPath.LIST_SYNC) {
    init {
        addOptions(CommandUtil.createOptionData(OptionType.STRING, CommandInfoPath.LIST_OPT_ID, true))
    }

    override fun run(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!

        //言語のデーター
        val langData = guild.getLang()

        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //同期をするように
        guildData.listSync = true

        //オプションを取得
        val option = event.getOption(CommandInfoPath.LIST_OPT_ID)

        //nullチェック
        if (option == null) {
            CommandUtil.replyCommandError(event)
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

        //ギルドのデータを保存
        SimpleTimer.instance.dataContainer.saveGuildsData(guild)
    }
}

/**
 * 同期を無効にする
 *
 */
object ListSyncOff : SlashCommandManager.SubCommand(CommandInfoPath.LIST_SYNC_OFF) {
    override fun run(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!

        //言語のデーター
        val langData = guild.getLang()

        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //同期をしないように
        guildData.listSync = false

        event.hook.sendMessage(langData.command.list.finishSync).queue()

        //ギルドのデータを保存
        SimpleTimer.instance.dataContainer.saveGuildsData(guild)
    }
}


/**
 * 一覧を他のサーバーと同期する
 *
 */
object CopyList : SlashCommandManager.SubCommand(CommandInfoPath.LIST_COPY) {
    init {
        addOptions(CommandUtil.createOptionData(OptionType.STRING, CommandInfoPath.LIST_OPT_ID, true))
    }

    override fun run(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!

        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //言語のデータ
        val langData = guild.getLang()

        //オプションを取得
        val option = event.getOption(CommandInfoPath.LIST_OPT_ID)

        //nullチェック
        if (option == null) {
            CommandUtil.replyCommandError(event)
            return
        }

        //Stringに変換
        val id = option.asString

        val long =
            try {
                //36進数にからLongにする
                java.lang.Long.parseLong(id, 36)
            } catch (e: NumberFormatException) {
                //エラー
                CommandUtil.replyCommandError(event)
                return
            }

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
object GetID : SlashCommandManager.SubCommand(CommandInfoPath.LIST_ID) {
    override fun run(event: SlashCommandInteractionEvent) {
        //メッセージを送信
        event.hook.sendMessage(event.guild!!.getLang().command.list.id.langFormat(event.guild!!.idLong.toString(36)))
            .queue()
    }
}