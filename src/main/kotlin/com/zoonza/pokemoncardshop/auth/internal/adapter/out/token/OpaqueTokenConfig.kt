package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpaqueTokenProperties::class)
class OpaqueTokenConfig {
}