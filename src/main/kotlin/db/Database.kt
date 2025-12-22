
package com.test.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*

object Database {
    lateinit var dataSource: HikariDataSource

    fun init(environment: ApplicationEnvironment) {
        val config = HikariConfig().apply {
            jdbcUrl = environment.config.property("ktor.database.url").getString()
            username = environment.config.property("ktor.database.user").getString()
            password = environment.config.property("ktor.database.password").getString()
            maximumPoolSize = 10
        }
        dataSource = HikariDataSource(config)
    }
}