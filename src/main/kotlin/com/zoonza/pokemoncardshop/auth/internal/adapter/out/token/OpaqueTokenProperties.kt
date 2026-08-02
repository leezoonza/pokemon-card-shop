package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.auth.token.refresh-token")
class OpaqueTokenProperties(
    val ttl: Duration
) {
}