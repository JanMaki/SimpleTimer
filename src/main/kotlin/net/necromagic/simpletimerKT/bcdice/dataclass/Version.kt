package net.necromagic.simpletimerKT.bcdice.dataclass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [/v2/version](https://github.com/bcdice/bcdice-api/blob/master/docs/api_v2.md#version)
 *
 * @property api [String] Apiのバージョン
 * @property bcdice [String] BCDiceのバージョン
 */
@Serializable
data class Version(
    @SerialName("api")
    val api: String,

    @SerialName("bcdice")
    val bcdice: String
)

