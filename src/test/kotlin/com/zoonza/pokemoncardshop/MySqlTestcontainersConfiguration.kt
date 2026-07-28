package com.zoonza.pokemoncardshop

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class MySqlTestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mySQLContainer(): MySQLContainer =
        MySQLContainer(DockerImageName.parse("mysql:8.4.9"))
            .withTmpFs(mapOf("/var/lib/mysql" to "rw"))
}