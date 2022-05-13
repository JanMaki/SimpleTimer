package dev.simpletimer.data.lang.lang_data.component

import kotlinx.serialization.Serializable

/**
 * コンポーネントの言語のデータ
 *
 * @property button ボタンの言語のデータ
 */
@Serializable
data class ComponentLangData (
    val button: ButtonLangData = ButtonLangData(),
    val modal: ModalLangData = ModalLangData()
)