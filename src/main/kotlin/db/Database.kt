
package com.test.db

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
    val dbUrl = System.getenv("DATABASE_URL")
        ?: "postgresql://postgres:ADjBovipIucWtyejVtBWoDSFfRvrwjsr@postgres.railway.internal:5432/railway"
//    val dbUrl = "postgresql://postgres:ADjBovipIucWtyejVtBWoDSFfRvrwjsr@postgres.railway.internal:5432/railway"
         val testVar = System.getenv("TEST_VAR")
    println("DATABASE_URL: $dbUrl")
    println("TEST_VAR: $testVar")

    val dataSource = if (dbUrl != null) {
        val regex = Regex("postgre(?:ql)?://(.+):(.+)@(.+):(\\d+)/(.+)")
        val match = regex.find(dbUrl)!!
        val (user, password, host, port, database) = match.destructured

        HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$host:$port/$database"
            driverClassName = "org.postgresql.Driver"
            username = user
            this.password = password
            maximumPoolSize = 5
        }.let {
            HikariDataSource(it)
        }
    } else {
        HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/testdb"
            driverClassName = "org.postgresql.Driver"
            username = "plnc_sy"
            password = ""
            maximumPoolSize = 5
        }.let {
            HikariDataSource(it)
        }
    }

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users)
    }
}