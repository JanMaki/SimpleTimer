package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OK(
    @SerialName("ok")
    val ok: Boolean,

    @SerialName("reason")
    val reason: String = "None"
)
