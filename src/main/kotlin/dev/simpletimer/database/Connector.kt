package dev.simpletimer.database

import dev.simpletimer.SimpleTimer
import dev.simpletimer.database.table.GuildDataTable
import dev.simpletimer.database.table.TimerDataTable
import dev.simpletimer.database.table.TimerMessageTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * DBへの接続に使用する
 */
object Connector {
    //データベースのコンフィグ
    private val databaseConfig = SimpleTimer.instance.dataContainer.config.databaseConfig

    /**
     * 接続
     *
     */
    fun connect() {
        //接続
        Database.connect(
            "jdbc:postgresql://${databaseConfig.address.value}/${databaseConfig.scheme.value}",
            "org.postgresql.Driver",
            databaseConfig.user.value,
            databaseConfig.password.value
        )

        transaction {
            //テーブルを作成
            createMissingTablesAndColumns(GuildDataTable, TimerDataTable, TimerMessageTable)
        }
    }
}