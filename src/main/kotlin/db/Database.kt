
package com.test.db

import com.test.db.Users.id
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object Users: Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id)
}

fun initDatabase() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
        driverClassName = "org.postgresql.Driver"
        username = "plnc_sy"
        password = ""
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users)
    }
}