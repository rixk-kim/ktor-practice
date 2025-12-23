
package com.test.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*

object Database {
    lateinit var dataSource: HikariDataSource

    fun init(environment: ApplicationEnvironment) {
        val dbConfig = environment.config.config("ktor.database")
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://${dbConfig.property("host").getString()}:${dbConfig.property("port").getString()}/${dbConfig.property("name").getString()}"
            username = dbConfig.property("user").getString()
            password = dbConfig.property("password").getString()
            maximumPoolSize = 10
        }
        dataSource = HikariDataSource(config)
    }
}