package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.auth.token.access-token")
class JwtTokenProperties(
    val secret: String,
    val ttl: Duration
)