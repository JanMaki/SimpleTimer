package dev.simpletimer.dice

import dev.simpletimer.data.lang.lang_data.LangData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.util.*

/**
 * ダイス用クラス
 * シークレットダイスはここでは処理しません。
 *
 * @param command [String] ダイスのコマンド。
 * @param secret [Boolean] シークレットダイスかどうか
 */
open class DefaultDice(val langData: LangData, private val command: String, secret: Boolean = false) {
    companion object {
        /**
         * 文字列がDiceのフォーマットに沿っているかを確認する
         *
         * @param string [String] 確認する文字列
         * @return [Boolean] 結果
         */
        fun checkDiceFormat(string: String): Boolean {
            var checkValue = string
            if (string.contains("<=")) {
                checkValue = string.substring(0, string.indexOf("<="))
            } else if (string.contains("<")) {
                checkValue = string.substring(0, string.indexOf("<"))
            }
            //ダイスの確認
            val splitted = checkValue.split("+")
            if (splitted.size > 10) {
                return false
            }
            val diceRegex = Regex("\\d{1,4}d\\d{1,4}")
            val numberRegex = Regex("\\d{1,4}")
            for (splittedString in splitted) {
                //形の確認
                if (!diceRegex.matches(splittedString) && !numberRegex.matches(splittedString)) {
                    return false
                }
            }
            return true
        }

        /**
         * ダイスの説明画面を表示
         *
         */
        fun getInfoEmbed(langData: LangData): MessageEmbed {
            //作成開始
            val builder = EmbedBuilder()
            builder.setTitle(langData.dice.defaultDice.title)
            builder.setColor(Color.GRAY)
            builder.addField("〇D●", langData.dice.defaultDice.dDescription, false)
            builder.addField("□+■", langData.dice.defaultDice.plusDescription, false)
            builder.addField("◇<◆", langData.dice.defaultDice.lessDescription, false)
            builder.addField(
                "◇<=◆",
                langData.dice.defaultDice.lessDescription,
                false
            )

            //送信
            return builder.build()
        }
    }

    //ダイスの結果をすべてまとめたリスト
    private val list = ArrayList<Int>()

    //結果の出力用の文字列
    val resultMessage: String

    //ランダムのインスタンス
    private val random = Random()

    init {
        //処理用に新しく代入
        var commandValue = command

        //目標値を確認
        var target = -1
        if (command.contains("<=")) {
            val targetString = command.substring(command.indexOf("<=") + 2)
            target = targetString.toIntOrNull() ?: 1
            commandValue = command.substring(0, command.indexOf("<="))
        } else if (command.contains("<")) {
            val targetString = command.substring(command.indexOf("<") + 1)
            target = targetString.toIntOrNull() ?: 1
            commandValue = command.substring(0, command.indexOf("<"))
        }

        //足し算ごとにダイスの処理をしていく
        val splitted = commandValue.split("+")
        val diceResultBuilder = StringBuilder()
        for ((index, short) in splitted.withIndex()) {

            //ダイスの結果を取得
            val resultList = dice(short)

            //２回目以上だった時は手前に+をつける
            if (index >= 1) {
                diceResultBuilder.append(" + ")
            }

            //ダイスの結果をつける
            diceResultBuilder.append(resultList.sum())

            //ダイス数が複数個あった時はかっことそれぞれの結果をつける
            if (resultList.size > 1) {
                val builder = StringBuilder()
                builder.append("[")
                for ((index2, result) in resultList.withIndex()) {
                    if (index2 >= 1) {
                        builder.append(",")
                    }
                    builder.append(result)
                }
                builder.append("]")
                diceResultBuilder.append(builder.toString())
            }
        }

        //1800文字以上の時は途中を省略する
        if (diceResultBuilder.length > 1800) {
            diceResultBuilder.clear()
            diceResultBuilder.append(langData.dice.defaultDice.omission)
        }

        //結果の文字列を作成
        resultMessage = "${if (secret) "||" else ""}${
            if (target > -1) {
                if (secret) {
                    if (list.sum() <= target) {
                        "\n$command　⇒　${diceResultBuilder}　⇒　${list.sum()}　⇒　${langData.dice.defaultDice.success}"
                    } else {
                        "\n$command　⇒　${diceResultBuilder}　⇒　${list.sum()}　⇒　${langData.dice.defaultDice.failure}"
                    }
                } else {
                    if (list.sum() <= target) {
                        "```diff\n+ $command　⇒　${diceResultBuilder}　⇒　${list.sum()}　⇒　${langData.dice.defaultDice.success}\n```"
                    } else {
                        "```diff\n- $command　⇒　${diceResultBuilder}　⇒　${list.sum()}　⇒　${langData.dice.defaultDice.failure}\n```"
                    }
                }
            } else {
                "\n$command　⇒　${diceResultBuilder}　⇒　**${list.sum()}**"
            }
        }${if (secret) "||" else ""}"
    }


    /**
     * ダイスを振る
     *
     * @param value [String] ダイスの文字列
     * @return [ArrayList]　ダイスの結果
     */
    private fun dice(value: String): ArrayList<Int> {
        //dで分けて、ダイスの個数と数値を分ける
        val splittedValue = value.split("d")
        val resultList = ArrayList<Int>()

        //もしdがなかった場合はそのまま返す
        if (splittedValue.size == 1) {
            resultList.add(splittedValue[0].toIntOrNull() ?: 0)
            list.add(splittedValue[0].toIntOrNull() ?: 0)
            return resultList
        }

        //ダイスを順番に振る
        for (count in 1..(splittedValue[0].toIntOrNull() ?: 1)) {
            //乱数作成
            val result = random.nextInt(splittedValue[1].toIntOrNull() ?: 1) + 1
            //個別の結果に追加
            resultList.add(result)
        }
        //全体の結果に追加
        list.addAll(resultList)

        //個別の結果を返す
        return resultList
    }
}