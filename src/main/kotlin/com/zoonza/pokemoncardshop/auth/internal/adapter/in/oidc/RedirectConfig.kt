package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(RedirectProperties::class)
class RedirectConfig {
}