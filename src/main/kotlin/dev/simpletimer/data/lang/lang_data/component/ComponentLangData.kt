package dev.simpletimer.data.lang.lang_data.component

import kotlinx.serialization.Serializable

@Serializable
data class ComponentLangData (
    val button: ButtonLangData = ButtonLangData()
)