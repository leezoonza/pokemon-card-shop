package com.zoonza.pokemoncardshop

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(
    MySqlTestcontainersConfiguration::class,
    RedisTestcontainersConfiguration::class
)
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
}
