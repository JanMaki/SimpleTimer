package dev.simpletimer.data.enum

/**
 * メンションの設定の列挙
 *
 */
enum class Mention {
    //メンションを行わない
    NONE,

    //@hereのメンションを行う
    HERE,

    //ロールにメンションを行う
    ROLE,

    //特定のVCにいるメンバーにメンションを行う
    TARGET_VC,

    //ボイスチャットにいるメンバーへメンションを行う
    VC
}
