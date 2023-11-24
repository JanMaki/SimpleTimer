package dev.simpletimer.database

import dev.simpletimer.SimpleTimer
import dev.simpletimer.database.table.GuildDataTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * DBへの接続に使用する
 */
object Connector {
    //初期化処理をしたかどうか
    private var initialized = false

    //データベースのコンフィグ
    private val databaseConfig = SimpleTimer.instance.dataContainer.config.databaseConfig

    /**
     * 接続
     *
     */
    fun connect() {
        //接続
        Database.connect(
            "jdbc:mariadb://${databaseConfig.address.value}/${databaseConfig.scheme.value}",
            "org.mariadb.jdbc.Driver",
            databaseConfig.user.value,
            databaseConfig.password.value
        )

        //初期化処理をしたかを確認
        if (!initialized) init()
    }

    /**
     * DBの初期化処理
     *
     */
    fun init() {
        initialized = true

        //テーブルを作成
        transaction {
            create(GuildDataTable)
            createMissingTablesAndColumns(GuildDataTable)
        }
    }
}